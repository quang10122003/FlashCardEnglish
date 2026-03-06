package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.TestFlashCard.FlashCard.entity.ToeicQuestionImage;
import com.TestFlashCard.FlashCard.entity.ToeicQuestionOption;
import com.TestFlashCard.FlashCard.repository.ExamRepository;
import com.TestFlashCard.FlashCard.repository.ToeicQuestionRepository;
import com.TestFlashCard.FlashCard.request.ToeicQuestionReorderRequest;
import com.TestFlashCard.FlashCard.request.ToeicQuestionReorderRequest.Item;
import com.TestFlashCard.FlashCard.request.ToeicQuestionRequestDTO;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.entity.Exam;
import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IExam_Repository;
import com.TestFlashCard.FlashCard.repository.IToeicQuestion_Repository;
import com.TestFlashCard.FlashCard.response.ToeicQuestionResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ToeicQuestionService {
    @Autowired
    private IToeicQuestion_Repository toeicQuestion_Repository;
    @Autowired
    private IExam_Repository exam_Repository;
    @Autowired
    private MinIO_MediaService minIO_MediaService;
    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private ToeicQuestionRepository toeicQuestionRepository;

    public ToeicQuestionResponse getById(int questionID) {
        ToeicQuestion question = toeicQuestion_Repository.findById(questionID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the toeic question with id: " + questionID));
        return convertQuestionToResponse(question);
    }

    public List<ToeicQuestionResponse> getByExamId(int examID) {
        Exam exam = exam_Repository.findById(examID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Exam with id : " + examID));

        List<ToeicQuestion> questions = toeicQuestion_Repository.findByExamId(exam.getId());
        return questions.stream().map(this::convertQuestionToResponse).toList();
    }

    public ToeicQuestionResponse convertQuestionToResponse(ToeicQuestion question) {

        // ---- Options ----
        List<ToeicQuestionResponse.OptionResponse> options = question.getOptions().stream()
                .map(opt -> new ToeicQuestionResponse.OptionResponse(
                        opt.getMark(),
                        opt.getDetail()))
                .collect(Collectors.toList());

        // ---- Images (new) ----
        List<String> imageUrls = question.getImages() != null
                ? question.getImages().stream()
                        .map(img -> minIO_MediaService.getPresignedURL(
                                img.getUrl(),
                                Duration.ofMinutes(1)))
                        .collect(Collectors.toList())
                : List.of();

        String audioUrlString = question.getAudio() != null
                ? minIO_MediaService.getPresignedURL(question.getAudio(), Duration.ofMinutes(1))
                : null;
        return new ToeicQuestionResponse(
                question.getId(),
                question.getIndexNumber(),
                question.getPart(),
                question.getDetail(),
                question.getResult(),
                imageUrls, // NEW FIELD
                question.getImages().stream().map(img -> img.getUrl()).toList(),
                audioUrlString,
                question.getAudio(),
                question.getConversation(),
                question.getClarify(),
                question.getIsContribute(),
                question.getBankQuestionId(),
                options);
    }

    @Transactional
    public ToeicQuestion createToeicQuestion(ToeicQuestionRequestDTO request) {

        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        ToeicQuestion question = new ToeicQuestion();
        question.setDetail(request.getDetail());
        question.setResult(request.getResult());
        question.setClarify(request.getClarify());
        if (request.getAudio() != null)
            question.setAudio(request.getAudio());
        question.setConversation(request.getConversation());
        question.setExam(exam);

        // ---- TỰ TÍNH INDEX ----
        int indexNumber;
        if (exam.isRandom()) {
            // Đề random: lấy max index trong đề
            Integer maxIndex = toeicQuestionRepository.findMaxIndexByExam(exam.getId());
            indexNumber = (maxIndex != null ? maxIndex : 0) + 1;
            // Không set part cho câu này
            question.setPart(null);
        } else {
            // TOEIC chuẩn: phải có part
            if (request.getPart() == null) {
                throw new RuntimeException("Part không được để trống cho đề TOEIC chuẩn");
            }
            question.setPart(request.getPart());

            int baseIndex = getStartIndexByPart(request.getPart());
            Integer maxIndexInPart = toeicQuestionRepository.findMaxIndexByExamAndPart(exam.getId(), request.getPart());
            indexNumber = (maxIndexInPart != null ? maxIndexInPart : baseIndex - 1) + 1;
        }
        question.setIndexNumber(indexNumber);

        // ---------- Options ----------
        List<ToeicQuestionOption> optionList = request.getOptions().stream()
                .map(o -> {
                    ToeicQuestionOption option = new ToeicQuestionOption();
                    option.setDetail(o.getDetail());
                    option.setMark(o.getMark());
                    option.setToeicQuestion(question);
                    return option;
                }).toList();
        question.setOptions(new HashSet<>(optionList));

        // ---------- Images ----------
        if (request.getImages() != null) {
            List<ToeicQuestionImage> imageList = request.getImages().stream()
                    .map(img -> {
                        ToeicQuestionImage image = new ToeicQuestionImage();
                        image.setUrl(img.getUrl());
                        image.setToeicQuestion(question);
                        return image;
                    }).toList();
            question.setImages(imageList);
        }

        return toeicQuestionRepository.save(question);
    }

    @Transactional
    public void deleteToeicQuestion(Integer questionId) {
        ToeicQuestion question = toeicQuestionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại"));

        // Xoá media trên MinIO
        if (question.getAudio() != null) {
            minIO_MediaService.deleteFile(question.getAudio());
        }

        if (question.getImages() != null) {
            question.getImages().forEach(img -> minIO_MediaService.deleteFile(img.getUrl()));
        }

        // Xoá câu hỏi
        toeicQuestionRepository.delete(question);
    }

    @Transactional
    public ToeicQuestion updateQuestion(Integer questionId, ToeicQuestionRequestDTO request) {
        ToeicQuestion question = toeicQuestionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại"));

        question.setDetail(request.getDetail());
        question.setResult(request.getResult());
        question.setClarify(request.getClarify());
        // ===== AUDIO UPDATE LOGIC CHUẨN =====

        // Case 1: user gửi audio = null → xoá audio
        if (request.getAudio() == null && question.getAudio() != null) {
            minIO_MediaService.deleteFile(question.getAudio());
            question.setAudio(null);
        }

        // Case 2: user upload audio mới
        else if (request.getAudio() != null && !request.getAudio().equals(question.getAudio())) {
            if (question.getAudio() != null) {
                minIO_MediaService.deleteFile(question.getAudio());
            }
            question.setAudio(request.getAudio());
        }

        // Case 3: request.getAudio() == null && question.getAudio() == null
        // → không làm gì cả (giữ nguyên)

        question.setConversation(request.getConversation());

        // ---- Options ----
        question.getOptions().clear();
        List<ToeicQuestionOption> newOptions = request.getOptions().stream().map(o -> {
            ToeicQuestionOption opt = new ToeicQuestionOption();
            opt.setDetail(o.getDetail());
            opt.setMark(o.getMark());
            opt.setToeicQuestion(question);
            return opt;
        }).toList();
        question.getOptions().addAll(newOptions);

        // ---- Images ----
        question.getImages().clear();
        if (request.getImages() != null) {
            List<ToeicQuestionImage> newImages = request.getImages().stream().map(img -> {
                ToeicQuestionImage image = new ToeicQuestionImage();
                image.setUrl(img.getUrl());
                image.setToeicQuestion(question);
                return image;
            }).toList();
            question.getImages().addAll(newImages);
        }

        return toeicQuestionRepository.save(question);
    }

    private int getStartIndexByPart(String part) {
        return switch (part) {
            case "1" -> 1;
            case "2" -> 7;
            case "3" -> 32;
            case "4" -> 71;
            case "5" -> 101;
            case "6" -> 131;
            case "7" -> 147;
            default -> 1;
        };
    }

    @Transactional
    public void reorder(ToeicQuestionReorderRequest req, Integer examId) throws BadRequestException {
        List<ToeicQuestion> questions;

        questions = toeicQuestion_Repository.findByExamIdAndPart(examId, req.getPart());

        Map<Integer, ToeicQuestion> map = questions.stream()
                .collect(Collectors.toMap(ToeicQuestion::getId, Function.identity()));

        for (Item it : req.getItems()) {
            ToeicQuestion q = map.get(it.getQuestionId());
            if (q == null)
                throw new BadRequestException("questionId not in exam/part");
            q.setIndexNumber(it.getIndexNumber());
        }

        toeicQuestion_Repository.saveAll(map.values());
    }
}
