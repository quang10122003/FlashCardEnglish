package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.entity.*;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.*;
import com.TestFlashCard.FlashCard.request.ExamSubmitRequest;
import com.TestFlashCard.FlashCard.request.ToeicQuestionRecord;
import com.TestFlashCard.FlashCard.response.ExamReviewResponse;
import com.TestFlashCard.FlashCard.response.QuestionReviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ExamReviewService {

    @Autowired
    private MinIO_MediaService minIO_MediaService;
    @Autowired
    private final IExamReview_Repository examReview_Repository;
    @Autowired
    private final IExam_Repository exam_Repository;
    @Autowired
    private final IToeicQuestion_Repository toeicQuestion_Repository;
    @Autowired
    private final AttempLogService attempLogService;

    // ==================== SUBMIT EXAM (UNIFIED) ====================

    /**
     * Submit exam - handles both TOEIC format and Disorder exam
     * 
     * TOEIC format: lấy câu hỏi theo selectedPart
     * Disorder: lấy TẤT CẢ câu hỏi của exam
     */
    @Transactional
    public ExamReviewResponse submitExam(ExamSubmitRequest request, User user) {
        if (request.getAnswers() == null) {
            request.setAnswers(new ArrayList<>());
        }

        Exam exam = exam_Repository.findById(request.getExamID())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        // Tăng số lần thi
        attempLogService.createAttemp(exam.getId(), user.getId());

        // ✅ LẤY CÂU HỎI THEO LOẠI EXAM
        List<ToeicQuestion> allQuestions = getQuestionsForExam(exam, request.getSelectedPart());

        // Khởi tạo ExamReview
        ExamReview examReview = new ExamReview();
        examReview.setExam(exam);
        examReview.setUser(user);
        examReview.setDuration(request.getDuration());

        // ✅ Set selectedPart phù hợp với loại exam
        String selectedPart = exam.isRandom() ? "ALL" : request.getSelectedPart();
        examReview.setSelectedPart(selectedPart);

        // Tính điểm
        int correctCount = 0;
        int incorrectCount = 0;
        List<QuestionReview> questionReviews = new ArrayList<>();

        // Map answers
        Map<Integer, Character> answerMap = new HashMap<>();
        for (ToeicQuestionRecord a : request.getAnswers()) {
            if (a == null)
                continue;
            Integer qid = a.getQuestionId();
            Character ans = a.getAnswer();
            if (qid == null || ans == null)
                continue;
            answerMap.putIfAbsent(qid, ans);
        }

        // So sánh và tính điểm
        for (ToeicQuestion question : allQuestions) {
            QuestionReview qr = new QuestionReview();

            Character userAnswer = answerMap.get(question.getId());
            boolean isCorrect = userAnswer != null
                    && question.getResult() != null
                    && question.getResult().equalsIgnoreCase(String.valueOf(userAnswer));

            if (isCorrect) {
                correctCount++;
            } else if (userAnswer != null) {
                incorrectCount++;
            }

            qr.setToeicQuestion(question);
            qr.setUserAnswer(userAnswer != null ? String.valueOf(userAnswer) : null);
            qr.setExamReview(examReview);

            questionReviews.add(qr);
        }

        // Gán và lưu
        examReview.setQuestionReviews(questionReviews);
        examReview.setIncorrect(incorrectCount);
        examReview.setResult(correctCount);
        examReview_Repository.save(examReview);
        exam_Repository.save(exam);

        // Tạo response
        return buildExamReviewResponse(examReview, exam, user, questionReviews);
    }

    // ==================== QUERY METHODS ====================

    /**
     * Lấy câu hỏi theo loại exam
     * - Disorder (isRandom=true): lấy tất cả câu hỏi
     * - TOEIC format (isRandom=false): lấy theo part đã chọn
     */
    private List<ToeicQuestion> getQuestionsForExam(Exam exam, String selectedPart) {
        if (exam.isRandom()) {
            // DISORDER: Lấy tất cả câu hỏi của exam
            return getQuestionsForDisorderExam(exam);
        } else {
            // TOEIC FORMAT: Lấy theo part
            return getQuestionsForToeicExam(exam, selectedPart);
        }
    }

    /**
     * Lấy TẤT CẢ câu hỏi của Disorder exam
     * Bao gồm cả single questions và questions trong group
     */
    private List<ToeicQuestion> getQuestionsForDisorderExam(Exam exam) {
        return toeicQuestion_Repository.findAllByExamOrderByIndexNumber(exam);
    }

    /**
     * Lấy câu hỏi của TOEIC exam theo part đã chọn
     * (Giữ logic cũ)
     */
    private List<ToeicQuestion> getQuestionsForToeicExam(Exam exam, String selectedPart) {
        Set<String> selected = Arrays.stream(selectedPart.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        return toeicQuestion_Repository.findAllByExamAndPartIn(exam, selected);
    }

    // ==================== GET RESULTS ====================

    /**
     * Lấy tất cả kết quả của user cho 1 exam
     */
    @Transactional
    public List<ExamReviewResponse> getAllExamResultByUser(User user, Exam exam) {
        List<ExamReview> resultList = examReview_Repository.findByUserAndExam(user, exam);
        return resultList.stream()
                .map(review -> convertToResponse(review, review.getExam(), review.getUser()))
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết 1 lần thi theo ID
     */
    public ExamReviewResponse getById(int id) {
        ExamReview review = examReview_Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find the Exam review with id: " + id));
        return convertToResponse(review, review.getExam(), review.getUser());
    }

    // ==================== RESPONSE BUILDERS ====================

    /**
     * Build response sau khi submit
     */
    private ExamReviewResponse buildExamReviewResponse(
            ExamReview examReview, Exam exam, User user, List<QuestionReview> questionReviews) {

        ExamReviewResponse response = new ExamReviewResponse();
        response.setReviewId(examReview.getId());
        response.setExamID(exam.getId());
        response.setUserID(user.getId());
        response.setExamTitle(exam.getTitle());
        response.setExamCollection(
                exam.getCollection() != null
                        ? exam.getCollection().getCollection()
                        : null);
        response.setUserName(user.getFullName());
        response.setDuration(examReview.getDuration());
        response.setCorrectAnswers(examReview.getResult());
        response.setIncorrectAnswers(examReview.getIncorrect());
        response.setNullAnswers(questionReviews.size() - examReview.getIncorrect() - examReview.getResult());
        response.setTotalQuestions(questionReviews.size());
        response.setCreatedAt(examReview.getCreateAt());
        response.setSelectedPart(examReview.getSelectedPart());

        // ✅ Section summary theo loại exam
        response.setSection(getSectionSummary(exam, questionReviews));

        // Map questions
        List<QuestionReviewResponse> questionReviewResponses = questionReviews.stream().map(qr -> {
            QuestionReviewResponse qrr = new QuestionReviewResponse();
            qrr.setQuestionId(qr.getToeicQuestion().getId());
            qrr.setUserAnswer(qr.getUserAnswer());
            qrr.setCorrectAnswer(qr.getToeicQuestion().getResult());
            qrr.setCorrect(qrr.getUserAnswer() != null &&
                    qrr.getUserAnswer().equalsIgnoreCase(qrr.getCorrectAnswer()));
            return qrr;
        }).toList();

        response.setQuestionReviews(questionReviewResponses);
        return response;
    }

    /**
     * Convert ExamReview entity to response DTO (full detail)
     */
    public ExamReviewResponse convertToResponse(ExamReview review, Exam exam, User user) {
        ExamReviewResponse response = new ExamReviewResponse();
        response.setReviewId(review.getId());
        response.setExamID(review.getExam().getId());
        response.setUserID(review.getUser().getId());
        response.setExamCollection(
                review.getExam().getCollection() != null
                        ? review.getExam().getCollection().getCollection()
                        : null);
        response.setExamTitle(review.getExam().getTitle());
        response.setUserName(review.getUser().getFullName());
        response.setDuration(review.getDuration());
        response.setCorrectAnswers(review.getResult());
        response.setIncorrectAnswers(review.getIncorrect());
        response.setNullAnswers(review.getQuestionReviews().size() - review.getResult() - review.getIncorrect());
        response.setTotalQuestions(review.getQuestionReviews().size());
        response.setSelectedPart(review.getSelectedPart());
        response.setCreatedAt(review.getCreateAt());

        // ✅ Section summary theo loại exam
        response.setSection(getSectionSummary(review.getExam(), review.getQuestionReviews()));

        response.setQuestionReviews(convertToQuestionsResponse(review.getQuestionReviews()));
        return response;
    }

    // ==================== SECTION SUMMARY ====================

    /**
     * Tạo section summary
     * - Disorder exam: trả về "Tự do"
     * - TOEIC exam: trả về "Part 1, Part 2, ..." hoặc "Toàn bộ"
     */
    private String getSectionSummary(Exam exam, List<QuestionReview> questionReviews) {
        // ✅ Disorder exam → trả về "Tự do"
        if (exam.isRandom()) {
            return "Tự do";
        }

        // TOEIC format → logic cũ
        return getPartSummaryFromReview(questionReviews);
    }

    /**
     * Get part summary string from review questions (TOEIC format only)
     */
    private String getPartSummaryFromReview(List<QuestionReview> reviewQuestions) {
        Set<Integer> partSet = reviewQuestions.stream()
                .map(rq -> rq.getToeicQuestion() != null ? rq.getToeicQuestion().getPart() : null)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::parsePartNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        boolean isFull = IntStream.rangeClosed(1, 7).allMatch(partSet::contains);
        if (isFull)
            return "Toàn bộ";
        if (partSet.isEmpty())
            return "N/A";

        return partSet.stream()
                .map(i -> "Part " + i)
                .collect(Collectors.joining(", "));
    }

    private Integer parsePartNumber(String partStr) {
        if (partStr == null || partStr.isBlank())
            return null;
        try {
            return Integer.parseInt(partStr.trim());
        } catch (NumberFormatException e) {
            try {
                String numStr = partStr.replaceAll("[^0-9]", "");
                if (!numStr.isEmpty()) {
                    return Integer.parseInt(numStr);
                }
            } catch (NumberFormatException e2) {
                // Ignore
            }
        }
        return null;
    }

    // ==================== QUESTION RESPONSE BUILDER ====================

    /**
     * Convert list of QuestionReview to response DTOs
     * Includes group information for questions that belong to a group
     */
    public List<QuestionReviewResponse> convertToQuestionsResponse(List<QuestionReview> questionReviews) {
        return questionReviews.stream().map(qr -> {
            QuestionReviewResponse qrr = new QuestionReviewResponse();
            ToeicQuestion question = qr.getToeicQuestion();

            // Images
            List<String> imageUrls = null;
            if (question.getImages() != null && !question.getImages().isEmpty()) {
                imageUrls = question.getImages().stream()
                        .map(img -> minIO_MediaService.getPresignedURL(img.getUrl(), Duration.ofDays(1)))
                        .collect(Collectors.toList());
            }

            // Audio
            String audio = null;
            if (question.getAudio() != null && !question.getAudio().isEmpty()) {
                audio = minIO_MediaService.getPresignedURL(question.getAudio(), Duration.ofDays(1));
            }

            // Basic fields
            qrr.setQuestionId(question.getId());
            qrr.setIndexNumber(question.getIndexNumber());
            qrr.setPart(question.getPart());
            qrr.setDetail(question.getDetail());
            qrr.setImages(imageUrls);
            qrr.setAudio(audio);
            qrr.setConversation(question.getConversation());
            qrr.setClarify(question.getClarify());
            qrr.setUserAnswer(qr.getUserAnswer());
            qrr.setCorrectAnswer(question.getResult());
            qrr.setCorrect(
                    qr.getUserAnswer() != null &&
                            qr.getUserAnswer().equalsIgnoreCase(question.getResult()));

            // Group information
            if (question.getGroup() != null) {
                GroupQuestion group = question.getGroup();
                qrr.setGroupId(group.getId());
                qrr.setGroupContent(group.getContent());
                qrr.setGroupQuestionRange(group.getQuestionRange());

                if (group.getImages() != null && !group.getImages().isEmpty()) {
                    List<String> groupImageUrls = group.getImages().stream()
                            .map(img -> minIO_MediaService.getPresignedURL(img.getUrl(), Duration.ofDays(1)))
                            .collect(Collectors.toList());
                    qrr.setGroupImages(groupImageUrls);
                }

                if (group.getAudios() != null && !group.getAudios().isEmpty()) {
                    List<String> groupAudioUrls = group.getAudios().stream()
                            .map(a -> minIO_MediaService.getPresignedURL(a.getUrl(), Duration.ofDays(1)))
                            .collect(Collectors.toList());
                    qrr.setGroupAudios(groupAudioUrls);
                }
            }

            // Options
            List<QuestionReviewResponse.OptionResponse> optionResponses = question.getOptions().stream().map(opt -> {
                QuestionReviewResponse.OptionResponse optRes = new QuestionReviewResponse.OptionResponse();
                optRes.setMark(opt.getMark());
                optRes.setDetail(opt.getDetail());
                return optRes;
            }).collect(Collectors.toList());

            qrr.setOptions(optionResponses);

            return qrr;
        }).collect(Collectors.toList());
    }

    // Backward compatible
    @Deprecated
    public ExamReviewResponse convertToRespone(ExamReview review, Exam exam, User user) {
        return convertToResponse(review, exam, user);
    }
}