package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.entity.*;
import com.TestFlashCard.FlashCard.repository.*;
import com.TestFlashCard.FlashCard.request.*;
import com.TestFlashCard.FlashCard.response.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisorderExamService {

    private final ExamRepository examRepository;
    private final ToeicQuestionRepository questionRepository;
    private final GroupQuestionRepository groupRepository;
    private final ToeicQuestionOptionRepository optionRepository;
    private final ToeicQuestionImageRepository questionImageRepository;
    private final GroupQuestionImageRepository groupImageRepository;
    private final GroupQuestionAudioRepository groupAudioRepository;
    private final MinIO_MediaService minIOMediaService;
    private final CustomExamRepository customExamRepository;

    // ==================== EXAM CRUD ====================

    @Transactional
    public DisorderExamResponse createDraft(User user) {
        // 1. Tạo exam
        Exam exam = new Exam();
        exam.setTitle("Đề thi mới");
        exam.setDuration(60);
        exam.setRandom(true);
        exam.setYear(LocalDateTime.now().getYear());

        examRepository.save(exam);

        // 2. Tạo quan hệ user - exam
        CustomExam customExam = new CustomExam();
        customExam.setUser(user);
        customExam.setCustomExam(exam);

        customExamRepository.save(customExam);

        return toDetailResponse(exam);
    }

    @Transactional(readOnly = true)
    public DisorderExamResponse getDetail(Integer examId, User user) {
        CustomExam customExam = customExamRepository
                .findByUser_IdAndCustomExam_Id(user.getId(), examId)
                .orElseThrow(() -> new RuntimeException("Bạn không sở hữu đề thi này"));

        Exam exam = customExam.getCustomExam();

        if (!exam.isRandom()) {
            throw new RuntimeException("Đây không phải đề thi ngẫu nhiên");
        }

        return toDetailResponse(exam);
    }

    @Transactional
    public DisorderExamResponse updateExam(Integer examId, User user, DisorderExamUpdateRequest request) {
        CustomExam customExam = customExamRepository
                .findByUser_IdAndCustomExam_Id(user.getId(), examId)
                .orElseThrow(() -> new RuntimeException("Bạn không sở hữu đề thi này"));

        Exam exam = customExam.getCustomExam();

        if (request.getTitle() != null) {
            exam.setTitle(request.getTitle());
        }
        if (request.getDuration() != null) {
            exam.setDuration(request.getDuration());
        }

        examRepository.save(exam);
        return toDetailResponse(exam);
    }

    @Transactional
    public void deleteExam(Integer examId, User user) {
        CustomExam customExam = customExamRepository
                .findByUser_IdAndCustomExam_Id(user.getId(), examId)
                .orElseThrow(() -> new RuntimeException("Bạn không sở hữu đề thi này"));

        Exam exam = customExam.getCustomExam();

        // Xóa media của tất cả câu hỏi
        List<ToeicQuestion> questions = questionRepository.findByExamId(examId);
        for (ToeicQuestion q : questions) {
            deleteQuestionMedia(q);
        }

        // Xóa media của tất cả groups
        List<GroupQuestion> groups = groupRepository.findByExamId(examId);
        for (GroupQuestion g : groups) {
            deleteGroupMedia(g);
        }

        // Xóa quan hệ
        customExamRepository.delete(customExam);

        // Xóa exam
        examRepository.delete(exam);
    }

    public List<DisorderExamListResponse> getMyDisorderExams(User user) {
        List<CustomExam> customExams = customExamRepository.findByUser_IdAndCustomExam_IsRandom(user.getId(), true);

        return customExams.stream()
                .map(CustomExam::getCustomExam)
                .map(exam -> new DisorderExamListResponse(
                        exam.getId(),
                        exam.getTitle(),
                        exam.getDuration(),
                        countTotalQuestions(exam.getId()),
                        exam.getCreatedAt()))
                .toList();
    }

    // ==================== SINGLE QUESTION ====================

    @Transactional
    public ToeicQuestionResponse addQuestion(Integer examId, DisorderQuestionRequest request) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Đề thi không tồn tại"));

        // Tính index mới
        Integer maxIndex = questionRepository.findMaxIndexByExam(examId);
        int newIndex = (maxIndex != null ? maxIndex : 0) + 1;

        ToeicQuestion question = new ToeicQuestion();
        question.setExam(exam);
        question.setGroup(null);
        question.setPart(null); // Đề ngẫu nhiên không có part
        question.setIndexNumber(newIndex);
        question.setDetail(request.getDetail());
        question.setResult(request.getResult());
        question.setClarify(request.getClarify());
        question.setAudio(request.getAudio());

        questionRepository.save(question);

        // Options
        if (request.getOptions() != null) {
            Set<ToeicQuestionOption> options = request.getOptions().stream()
                    .map(o -> {
                        ToeicQuestionOption opt = new ToeicQuestionOption();
                        opt.setMark(o.getMark());
                        opt.setDetail(o.getDetail());
                        opt.setToeicQuestion(question);
                        return opt;
                    })
                    .collect(Collectors.toSet());
            optionRepository.saveAll(options);
            question.setOptions(options);
        }

        // Images
        if (request.getImages() != null) {
            List<ToeicQuestionImage> images = request.getImages().stream()
                    .map(img -> {
                        ToeicQuestionImage i = new ToeicQuestionImage();
                        i.setUrl(img.getUrl());
                        i.setToeicQuestion(question);
                        return i;
                    })
                    .collect(Collectors.toList());
            questionImageRepository.saveAll(images);
            question.setImages(images);
        }

        return toQuestionResponse(question);
    }

    @Transactional
    public ToeicQuestionResponse updateQuestion(Integer examId, Integer questionId, DisorderQuestionRequest request) {
        ToeicQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại"));

        if (!question.getExam().getId().equals(examId)) {
            throw new RuntimeException("Câu hỏi không thuộc đề thi này");
        }

        question.setDetail(request.getDetail());
        question.setResult(request.getResult());
        question.setClarify(request.getClarify());

        // Audio update
        if (request.getAudio() == null && question.getAudio() != null) {
            minIOMediaService.deleteFile(question.getAudio());
            question.setAudio(null);
        } else if (request.getAudio() != null && !request.getAudio().equals(question.getAudio())) {
            if (question.getAudio() != null) {
                minIOMediaService.deleteFile(question.getAudio());
            }
            question.setAudio(request.getAudio());
        }

        // Options update
        question.getOptions().clear();
        if (request.getOptions() != null) {
            Set<ToeicQuestionOption> options = request.getOptions().stream()
                    .map(o -> {
                        ToeicQuestionOption opt = new ToeicQuestionOption();
                        opt.setMark(o.getMark());
                        opt.setDetail(o.getDetail());
                        opt.setToeicQuestion(question);
                        return opt;
                    })
                    .collect(Collectors.toSet());
            question.getOptions().addAll(options);
        }

        // Images update
        question.getImages().clear();
        if (request.getImages() != null) {
            List<ToeicQuestionImage> images = request.getImages().stream()
                    .map(img -> {
                        ToeicQuestionImage i = new ToeicQuestionImage();
                        i.setUrl(img.getUrl());
                        i.setToeicQuestion(question);
                        return i;
                    })
                    .collect(Collectors.toList());
            question.getImages().addAll(images);
        }

        questionRepository.save(question);
        return toQuestionResponse(question);
    }

    @Transactional
    public void deleteQuestion(Integer examId, Integer questionId) {
        ToeicQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại"));

        if (!question.getExam().getId().equals(examId)) {
            throw new RuntimeException("Câu hỏi không thuộc đề thi này");
        }

        deleteQuestionMedia(question);
        questionRepository.delete(question);

        // Recalculate indexes
        recalculateIndexes(examId);
    }

    // ==================== GROUP QUESTION ====================

    @Transactional
    public GroupQuestionResponseDTO addGroup(Integer examId, DisorderGroupRequest request) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Đề thi không tồn tại"));

        GroupQuestion group = new GroupQuestion();
        group.setExam(exam);
        group.setPart(""); // Đề ngẫu nhiên: part = "" (empty string vì nullable=false)
        group.setTitle(request.getTitle());
        group.setContent(request.getContent());

        groupRepository.save(group);

        // Group Images
        if (request.getImages() != null) {
            Set<GroupQuestionImage> images = request.getImages().stream()
                    .map(img -> {
                        GroupQuestionImage i = new GroupQuestionImage();
                        i.setUrl(img.getUrl());
                        i.setGroup(group);
                        return i;
                    })
                    .collect(Collectors.toSet());
            groupImageRepository.saveAll(images);
            group.setImages(images);
        }

        // Group Audios
        if (request.getAudios() != null) {
            Set<GroupQuestionAudio> audios = request.getAudios().stream()
                    .map(a -> {
                        GroupQuestionAudio audio = new GroupQuestionAudio();
                        audio.setUrl(a.getUrl());
                        audio.setGroup(group);
                        return audio;
                    })
                    .collect(Collectors.toSet());
            groupAudioRepository.saveAll(audios);
            group.setAudios(audios);
        }

        // Child Questions
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            Integer maxIndex = questionRepository.findMaxIndexByExam(examId);
            int currentIndex = (maxIndex != null ? maxIndex : 0) + 1;

            for (DisorderQuestionRequest qReq : request.getQuestions()) {
                ToeicQuestion q = new ToeicQuestion();
                q.setExam(exam);
                q.setGroup(group);
                q.setPart(null);
                q.setIndexNumber(currentIndex++);
                q.setDetail(qReq.getDetail());
                q.setResult(qReq.getResult());
                q.setClarify(qReq.getClarify());

                questionRepository.save(q);

                // Options
                if (qReq.getOptions() != null) {
                    Set<ToeicQuestionOption> options = qReq.getOptions().stream()
                            .map(o -> {
                                ToeicQuestionOption opt = new ToeicQuestionOption();
                                opt.setMark(o.getMark());
                                opt.setDetail(o.getDetail());
                                opt.setToeicQuestion(q);
                                return opt;
                            })
                            .collect(Collectors.toSet());
                    optionRepository.saveAll(options);
                    q.setOptions(options);
                }

                // Images
                if (qReq.getImages() != null) {
                    List<ToeicQuestionImage> images = qReq.getImages().stream()
                            .map(img -> {
                                ToeicQuestionImage i = new ToeicQuestionImage();
                                i.setUrl(img.getUrl());
                                i.setToeicQuestion(q);
                                return i;
                            })
                            .collect(Collectors.toList());
                    questionImageRepository.saveAll(images);
                    q.setImages(images);
                }

                group.getQuestions().add(q);
            }

            // Update question range
            updateGroupQuestionRange(group);
        }

        groupRepository.save(group);

        return toGroupResponse(group);
    }

    @Transactional
    public GroupQuestionResponseDTO updateGroup(Integer examId, Integer groupId, DisorderGroupRequest request) {
        GroupQuestion group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm câu hỏi không tồn tại"));

        if (!group.getExam().getId().equals(examId)) {
            throw new RuntimeException("Nhóm không thuộc đề thi này");
        }

        if (request.getTitle() != null) {
            group.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            group.setContent(request.getContent());
        }

        // Update images nếu có
        if (request.getImages() != null) {
            // Xóa ảnh cũ không còn trong request
            Set<String> newKeys = request.getImages().stream()
                    .map(DisorderGroupRequest.ImageRequest::getUrl)
                    .collect(Collectors.toSet());

            group.getImages().removeIf(img -> {
                if (!newKeys.contains(img.getUrl())) {
                    minIOMediaService.deleteFile(img.getUrl());
                    return true;
                }
                return false;
            });

            // Thêm ảnh mới
            Set<String> existingKeys = group.getImages().stream()
                    .map(GroupQuestionImage::getUrl)
                    .collect(Collectors.toSet());

            for (var img : request.getImages()) {
                if (!existingKeys.contains(img.getUrl())) {
                    GroupQuestionImage i = new GroupQuestionImage();
                    i.setUrl(img.getUrl());
                    i.setGroup(group);
                    group.getImages().add(i);
                }
            }
        }

        // Update audios nếu có
        if (request.getAudios() != null) {
            Set<String> newKeys = request.getAudios().stream()
                    .map(DisorderGroupRequest.AudioRequest::getUrl)
                    .collect(Collectors.toSet());

            group.getAudios().removeIf(a -> {
                if (!newKeys.contains(a.getUrl())) {
                    minIOMediaService.deleteFile(a.getUrl());
                    return true;
                }
                return false;
            });

            Set<String> existingKeys = group.getAudios().stream()
                    .map(GroupQuestionAudio::getUrl)
                    .collect(Collectors.toSet());

            for (var a : request.getAudios()) {
                if (!existingKeys.contains(a.getUrl())) {
                    GroupQuestionAudio audio = new GroupQuestionAudio();
                    audio.setUrl(a.getUrl());
                    audio.setGroup(group);
                    group.getAudios().add(audio);
                }
            }
        }

        groupRepository.save(group);
        return toGroupResponse(group);
    }

    @Transactional
    public void deleteGroup(Integer examId, Integer groupId) {
        GroupQuestion group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm câu hỏi không tồn tại"));

        if (!group.getExam().getId().equals(examId)) {
            throw new RuntimeException("Nhóm không thuộc đề thi này");
        }

        deleteGroupMedia(group);
        groupRepository.delete(group);

        recalculateIndexes(examId);
    }

    @Transactional
    public ToeicQuestionResponse addQuestionToGroup(Integer examId, Integer groupId, DisorderQuestionRequest request) {
        GroupQuestion group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Nhóm câu hỏi không tồn tại"));

        if (!group.getExam().getId().equals(examId)) {
            throw new RuntimeException("Nhóm không thuộc đề thi này");
        }

        // Tính index mới
        Integer maxIndex = questionRepository.findMaxIndexByExam(examId);
        int newIndex = (maxIndex != null ? maxIndex : 0) + 1;

        ToeicQuestion question = new ToeicQuestion();
        question.setExam(group.getExam());
        question.setGroup(group);
        question.setPart(null);
        question.setIndexNumber(newIndex);
        question.setDetail(request.getDetail());
        question.setResult(request.getResult());
        question.setClarify(request.getClarify());

        questionRepository.save(question);

        // Options
        if (request.getOptions() != null) {
            Set<ToeicQuestionOption> options = request.getOptions().stream()
                    .map(o -> {
                        ToeicQuestionOption opt = new ToeicQuestionOption();
                        opt.setMark(o.getMark());
                        opt.setDetail(o.getDetail());
                        opt.setToeicQuestion(question);
                        return opt;
                    })
                    .collect(Collectors.toSet());
            optionRepository.saveAll(options);
            question.setOptions(options);
        }

        // Images
        if (request.getImages() != null) {
            List<ToeicQuestionImage> images = request.getImages().stream()
                    .map(img -> {
                        ToeicQuestionImage i = new ToeicQuestionImage();
                        i.setUrl(img.getUrl());
                        i.setToeicQuestion(question);
                        return i;
                    })
                    .collect(Collectors.toList());
            questionImageRepository.saveAll(images);
            question.setImages(images);
        }

        group.getQuestions().add(question);
        updateGroupQuestionRange(group);
        groupRepository.save(group);

        return toQuestionResponse(question);
    }

    // ==================== REORDER ====================

    @Transactional
    public void reorderItems(Integer examId, DisorderReorderRequest request) {
        for (DisorderReorderRequest.ReorderItem item : request.getItems()) {
            if ("single".equals(item.getType())) {
                ToeicQuestion q = questionRepository.findById(item.getId())
                        .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại: " + item.getId()));
                q.setIndexNumber(item.getDisplayOrder());
                questionRepository.save(q);
            }
            // Group không cần update index vì nó dựa vào câu con
        }

        // Update question range cho tất cả groups
        List<GroupQuestion> groups = groupRepository.findByExamId(examId);
        for (GroupQuestion g : groups) {
            updateGroupQuestionRange(g);
            groupRepository.save(g);
        }
    }

    // ==================== HELPER METHODS ====================

    private int countTotalQuestions(Integer examId) {
        return questionRepository.countByExam_Id(examId);
    }

    private void recalculateIndexes(Integer examId) {
        // Lấy tất cả câu hỏi và sort theo indexNumber
        List<ToeicQuestion> questions = questionRepository.findByExamId(examId);
        questions.sort(Comparator.comparingInt(q -> q.getIndexNumber() != null ? q.getIndexNumber() : 0));

        int index = 1;
        for (ToeicQuestion q : questions) {
            q.setIndexNumber(index++);
            questionRepository.save(q);
        }

        // Update question range cho groups
        List<GroupQuestion> groups = groupRepository.findByExamId(examId);
        for (GroupQuestion g : groups) {
            updateGroupQuestionRange(g);
            groupRepository.save(g);
        }
    }

    private void updateGroupQuestionRange(GroupQuestion group) {
        List<ToeicQuestion> qs = group.getQuestions();
        if (qs == null || qs.isEmpty()) {
            group.setQuestionRange("");
            group.setTitle("Nhóm câu hỏi trống");
            return;
        }

        int min = qs.stream().mapToInt(ToeicQuestion::getIndexNumber).min().orElse(0);
        int max = qs.stream().mapToInt(ToeicQuestion::getIndexNumber).max().orElse(0);

        group.setQuestionRange(min + "-" + max);
        if (group.getTitle() == null || group.getTitle().isEmpty()) {
            group.setTitle("Câu " + min + " - " + max);
        }
    }

    private void deleteQuestionMedia(ToeicQuestion q) {
        if (q.getAudio() != null) {
            minIOMediaService.deleteFile(q.getAudio());
        }
        if (q.getImages() != null) {
            for (ToeicQuestionImage img : q.getImages()) {
                minIOMediaService.deleteFile(img.getUrl());
            }
        }
    }

    private void deleteGroupMedia(GroupQuestion g) {
        if (g.getImages() != null) {
            for (GroupQuestionImage img : g.getImages()) {
                minIOMediaService.deleteFile(img.getUrl());
            }
        }
        if (g.getAudios() != null) {
            for (GroupQuestionAudio a : g.getAudios()) {
                minIOMediaService.deleteFile(a.getUrl());
            }
        }
        if (g.getQuestions() != null) {
            for (ToeicQuestion q : g.getQuestions()) {
                deleteQuestionMedia(q);
            }
        }
    }

    // ==================== RESPONSE MAPPING ====================

    private DisorderExamResponse toDetailResponse(Exam exam) {
        List<DisorderItemResponse> items = new ArrayList<>();

        // Lấy tất cả câu hỏi của exam và filter câu đơn (không thuộc group)
        List<ToeicQuestion> allQuestions = questionRepository.findByExamId(exam.getId());
        List<ToeicQuestion> singleQuestions = allQuestions.stream()
                .filter(q -> q.getGroup() == null)
                .toList();

        for (ToeicQuestion q : singleQuestions) {
            items.add(toSingleItemResponse(q));
        }

        // Lấy groups
        List<GroupQuestion> groups = groupRepository.findByExamId(exam.getId());
        for (GroupQuestion g : groups) {
            items.add(toGroupItemResponse(g));
        }

        // Sort theo displayOrder (dựa vào indexNumber của câu đầu tiên)
        items.sort(Comparator.comparingInt(DisorderItemResponse::getDisplayOrder));

        int total = countTotalQuestions(exam.getId());

        return new DisorderExamResponse(
                exam.getId(),
                exam.getTitle(),
                exam.getDuration(),
                total,
                exam.getCreatedAt(),
                items);
    }

    private DisorderItemResponse toSingleItemResponse(ToeicQuestion q) {
        return DisorderItemResponse.builder()
                .id(q.getId())
                .type("single")
                .displayOrder(q.getIndexNumber())
                .detail(q.getDetail())
                .result(q.getResult())
                .clarify(q.getClarify())
                .images(q.getImages() != null ? q.getImages().stream()
                        .map(img -> minIOMediaService.getPresignedURL(img.getUrl(), Duration.ofDays(1)))
                        .collect(Collectors.toList()) : List.of())
                .imageKeys(q.getImages() != null ? q.getImages().stream()
                        .map(ToeicQuestionImage::getUrl)
                        .collect(Collectors.toList()) : List.of())
                .audio(q.getAudio() != null ? minIOMediaService.getPresignedURL(q.getAudio(), Duration.ofDays(1))
                        : null)
                .audioKey(q.getAudio())
                .options(q.getOptions() != null ? q.getOptions().stream()
                        .map(o -> new DisorderItemResponse.OptionResponse(o.getMark(), o.getDetail()))
                        .collect(Collectors.toList()) : List.of())
                .isContribute(q.getIsContribute())
                .bankQuestionId(q.getBankQuestionId())
                .build();
    }

    private DisorderItemResponse toGroupItemResponse(GroupQuestion g) {
        int displayOrder = g.getQuestions() != null && !g.getQuestions().isEmpty()
                ? g.getQuestions().stream().mapToInt(ToeicQuestion::getIndexNumber).min().orElse(0)
                : 0; // ✅ FIX: Dùng 0 thay vì Integer.MAX_VALUE để tránh lỗi sort

        List<DisorderItemResponse.ChildQuestionResponse> children = g.getQuestions() != null
                ? g.getQuestions().stream()
                        .sorted(Comparator.comparingInt(ToeicQuestion::getIndexNumber))
                        .map(q -> new DisorderItemResponse.ChildQuestionResponse(
                                q.getId(),
                                q.getIndexNumber(),
                                q.getDetail(),
                                q.getResult(),
                                q.getClarify(),
                                // Images URLs
                                q.getImages() != null ? q.getImages().stream()
                                        .map(img -> minIOMediaService.getPresignedURL(img.getUrl(), Duration.ofDays(1)))
                                        .collect(Collectors.toList()) : List.of(),
                                // Image Keys
                                q.getImages() != null ? q.getImages().stream()
                                        .map(ToeicQuestionImage::getUrl)
                                        .collect(Collectors.toList()) : List.of(),
                                // ✅ NEW: Audio URL (nullable cho child question)
                                q.getAudio() != null && !q.getAudio().isEmpty()
                                        ? minIOMediaService.getPresignedURL(q.getAudio(), Duration.ofDays(1))
                                        : null,
                                // ✅ NEW: Audio Key
                                q.getAudio(),
                                // Options
                                q.getOptions() != null ? q.getOptions().stream()
                                        .map(o -> new DisorderItemResponse.OptionResponse(o.getMark(), o.getDetail()))
                                        .collect(Collectors.toList()) : List.of()))
                        .collect(Collectors.toList())
                : List.of();

        return DisorderItemResponse.builder()
                .id(g.getId())
                .type("group")
                .displayOrder(displayOrder)
                .title(g.getTitle())
                .content(g.getContent())
                .questionRange(g.getQuestionRange())
                .groupImages(g.getImages() != null ? g.getImages().stream()
                        .map(img -> minIOMediaService.getPresignedURL(img.getUrl(), Duration.ofDays(1)))
                        .collect(Collectors.toList()) : List.of())
                .groupImageKeys(g.getImages() != null ? g.getImages().stream()
                        .map(GroupQuestionImage::getUrl)
                        .collect(Collectors.toList()) : List.of())
                .groupAudios(g.getAudios() != null ? g.getAudios().stream()
                        .map(a -> minIOMediaService.getPresignedURL(a.getUrl(), Duration.ofDays(1)))
                        .collect(Collectors.toList()) : List.of())
                .groupAudioKeys(g.getAudios() != null ? g.getAudios().stream()
                        .map(GroupQuestionAudio::getUrl)
                        .collect(Collectors.toList()) : List.of())
                .questions(children)
                .groupIsContribute(g.getIsContribute())
                .bankGroupId(g.getBankGroupId())
                .build();
    }

    private ToeicQuestionResponse toQuestionResponse(ToeicQuestion q) {
        List<ToeicQuestionResponse.OptionResponse> options = q.getOptions() != null
                ? q.getOptions().stream()
                        .map(o -> new ToeicQuestionResponse.OptionResponse(o.getMark(), o.getDetail()))
                        .collect(Collectors.toList())
                : List.of();

        List<String> imageUrls = q.getImages() != null
                ? q.getImages().stream()
                        .map(img -> minIOMediaService.getPresignedURL(img.getUrl(), Duration.ofDays(1)))
                        .collect(Collectors.toList())
                : List.of();

        List<String> imageKeys = q.getImages() != null
                ? q.getImages().stream()
                        .map(ToeicQuestionImage::getUrl)
                        .collect(Collectors.toList())
                : List.of();

        String audioUrl = q.getAudio() != null
                ? minIOMediaService.getPresignedURL(q.getAudio(), Duration.ofDays(1))
                : null;

        return new ToeicQuestionResponse(
                q.getId(),
                q.getIndexNumber(),
                q.getPart(),
                q.getDetail(),
                q.getResult(),
                imageUrls,
                imageKeys,
                audioUrl,
                q.getAudio(),
                q.getConversation(),
                q.getClarify(),
                q.getIsContribute(),
                q.getBankQuestionId(),
                options);
    }

    private GroupQuestionResponseDTO toGroupResponse(GroupQuestion group) {
        GroupQuestionResponseDTO dto = new GroupQuestionResponseDTO();
        dto.setId(group.getId());
        dto.setPart(group.getPart());
        dto.setTitle(group.getTitle());
        dto.setContent(group.getContent());
        dto.setQuestionRange(group.getQuestionRange());
        dto.setExamId(group.getExam().getId());
        dto.setIsContribute(group.getIsContribute());
        dto.setBankGroupId(group.getBankGroupId());

        dto.setImages(group.getImages() != null
                ? group.getImages().stream()
                        .map(img -> minIOMediaService.getPresignedURL(img.getUrl(), Duration.ofDays(1)))
                        .collect(Collectors.toList())
                : List.of());

        dto.setImageKeys(group.getImages() != null
                ? group.getImages().stream()
                        .map(GroupQuestionImage::getUrl)
                        .collect(Collectors.toList())
                : List.of());

        dto.setAudios(group.getAudios() != null
                ? group.getAudios().stream()
                        .map(a -> minIOMediaService.getPresignedURL(a.getUrl(), Duration.ofDays(1)))
                        .collect(Collectors.toList())
                : List.of());

        dto.setAudioKeys(group.getAudios() != null
                ? group.getAudios().stream()
                        .map(GroupQuestionAudio::getUrl)
                        .collect(Collectors.toList())
                : List.of());

        dto.setQuestions(group.getQuestions() != null
                ? group.getQuestions().stream()
                        .map(this::toQuestionResponse)
                        .collect(Collectors.toList())
                : List.of());

        return dto;
    }
}