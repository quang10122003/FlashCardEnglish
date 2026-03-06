package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.entity.*;
import com.TestFlashCard.FlashCard.repository.ExamRepository;
import com.TestFlashCard.FlashCard.repository.GroupQuestionRepository;
import com.TestFlashCard.FlashCard.repository.ToeicQuestionRepository;
import com.TestFlashCard.FlashCard.request.GroupQuestionRequestDTO;
import com.TestFlashCard.FlashCard.request.ToeicQuestionForGroupRequestDTO;
import com.TestFlashCard.FlashCard.request.ToeicQuestionRequestDTO;
import com.TestFlashCard.FlashCard.response.GroupQuestionResponseDTO;
import com.TestFlashCard.FlashCard.response.ToeicQuestionResponse;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupQuestionService {

    private final ExamRepository examRepository;
    private final GroupQuestionRepository groupRepo;
    private final ToeicQuestionRepository questionRepo;
    private final MinIO_MediaService minIO_MediaService;
    private final ToeicQuestionRepository toeicQuestionRepository;

    @Transactional
    public GroupQuestionResponseDTO createGroup(GroupQuestionRequestDTO req) {

        Exam exam = examRepository.findById(req.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam không tồn tại"));

        GroupQuestion group = new GroupQuestion();
        group.setPart(req.getPart());
        group.setTitle(req.getTitle());
        group.setQuestionRange(req.getQuestionRange());
        group.setContent(req.getContent());
        group.setExam(exam);

        // Images
        if (req.getImages() != null) {
            for (var img : req.getImages()) {
                GroupQuestionImage i = new GroupQuestionImage();
                i.setUrl(img.getUrl());
                i.setGroup(group);
                group.getImages().add(i);
            }
        }

        // Audios
        if (req.getAudios() != null) {
            for (var audio : req.getAudios()) {
                GroupQuestionAudio a = new GroupQuestionAudio();
                a.setUrl(audio.getUrl());
                a.setGroup(group);
                group.getAudios().add(a);
            }
        }

        // Child questions
        if (req.getQuestions() != null) {
            for (ToeicQuestionRequestDTO qReq : req.getQuestions()) {
                ToeicQuestion q = new ToeicQuestion();
                q.setDetail(qReq.getDetail());
                q.setIndexNumber(qReq.getIndexNumber());
                q.setResult(qReq.getResult());
                q.setClarify(qReq.getClarify());
                q.setAudio(qReq.getAudio());
                q.setExam(exam);
                q.setPart(req.getPart());
                q.setGroup(group);

                // Options
                q.setOptions(new HashSet<>(
                        qReq.getOptions().stream().map(o -> {
                            ToeicQuestionOption opt = new ToeicQuestionOption();
                            opt.setDetail(o.getDetail());
                            opt.setMark(o.getMark());
                            opt.setToeicQuestion(q);
                            return opt;
                        }).toList()));

                // Images
                if (qReq.getImages() != null) {
                    q.setImages(
                            qReq.getImages().stream().map(img -> {
                                ToeicQuestionImage qi = new ToeicQuestionImage();
                                qi.setUrl(img.getUrl());
                                qi.setToeicQuestion(q);
                                return qi;
                            }).toList());
                }

                group.getQuestions().add(q);
            }
        }

        GroupQuestion saved = groupRepo.save(group);
        groupRepo.flush(); // optional nhưng nên có

        GroupQuestion fresh = groupRepo.findById(saved.getId())
                .orElseThrow();
        return toResponseDTO(fresh);
    }

    @Transactional
    public GroupQuestionResponseDTO updateGroup(Integer groupId, GroupQuestionRequestDTO req) {

        GroupQuestion group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group không tồn tại"));

        // Update basic fields
        if (req.getPart() != null) {
            group.setPart(req.getPart());
        }
        if (req.getTitle() != null) {
            group.setTitle(req.getTitle());
        }
        if (req.getQuestionRange() != null) {
            group.setQuestionRange(req.getQuestionRange());
        }
        if (req.getContent() != null) {
            group.setContent(req.getContent());
        }

        // --- Update images (chỉ khi request có gửi images) ---
        if (req.getImages() != null) {
            // Xóa ảnh cũ không còn trong request
            if (group.getImages() != null) {
                List<String> newKeys = req.getImages().stream()
                        .map(img -> img.getUrl())
                        .toList();

                // Xóa những ảnh không còn trong danh sách mới
                List<GroupQuestionImage> toRemove = group.getImages().stream()
                        .filter(img -> !newKeys.contains(img.getUrl()))
                        .toList();

                for (var img : toRemove) {
                    minIO_MediaService.deleteFile(img.getUrl());
                    group.getImages().remove(img);
                }
            }

            // Thêm ảnh mới (những key chưa có)
            List<String> existingKeys = group.getImages().stream()
                    .map(img -> img.getUrl())
                    .toList();

            for (var img : req.getImages()) {
                if (!existingKeys.contains(img.getUrl())) {
                    GroupQuestionImage i = new GroupQuestionImage();
                    i.setUrl(img.getUrl());
                    i.setGroup(group);
                    group.getImages().add(i);
                }
            }
        }

        // --- Update audios (chỉ khi request có gửi audios) ---
        if (req.getAudios() != null) {
            // Xóa audio cũ không còn trong request
            if (group.getAudios() != null) {
                List<String> newKeys = req.getAudios().stream()
                        .map(audio -> audio.getUrl())
                        .toList();

                List<GroupQuestionAudio> toRemove = group.getAudios().stream()
                        .filter(audio -> !newKeys.contains(audio.getUrl()))
                        .toList();

                for (var audio : toRemove) {
                    minIO_MediaService.deleteFile(audio.getUrl());
                    group.getAudios().remove(audio);
                }
            }

            // Thêm audio mới
            List<String> existingKeys = group.getAudios().stream()
                    .map(audio -> audio.getUrl())
                    .toList();

            for (var audio : req.getAudios()) {
                if (!existingKeys.contains(audio.getUrl())) {
                    GroupQuestionAudio a = new GroupQuestionAudio();
                    a.setUrl(audio.getUrl());
                    a.setGroup(group);
                    group.getAudios().add(a);
                }
            }
        }

        // --- KHÔNG xóa questions ở đây ---
        // Questions được quản lý riêng qua ToeicQuestionController

        return toResponseDTO(groupRepo.save(group));
    }

    // Hàm helper: parse "32-35" -> 32
    private int extractStartIndex(String range) {
        try {
            return Integer.parseInt(range.split("-")[0].trim());
        } catch (Exception e) {
            return 1; // default
        }
    }

    @Transactional
    public GroupQuestionResponseDTO addGroupToQuestion(Integer questionId, Integer groupId) {
        GroupQuestion group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group không tồn tại"));

        ToeicQuestion q = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("ToeicQuestion không tồn tại"));

        // Remove from old group (two-way)
        if (q.getGroup() != null && q.getGroup().getId() != null) {
            q.getGroup().getQuestions().removeIf(qq -> qq.getId().equals(q.getId()));
        }

        q.setGroup(group);
        q.setExam(group.getExam());
        q.setPart(group.getPart());
        questionRepo.save(q);

        if (group.getQuestions() == null) {
            group.setQuestions(new java.util.ArrayList<>());
        }
        if (group.getQuestions().stream().noneMatch(qq -> qq.getId().equals(q.getId()))) {
            group.getQuestions().add(q);
        }

        return toResponseDTO(groupRepo.save(group));
    }

    @Transactional
    public void deleteGroup(Integer id) {
        GroupQuestion group = groupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Group không tồn tại"));

        // Xóa media ảnh của group
        if (group.getImages() != null) {
            for (GroupQuestionImage img : group.getImages()) {
                minIO_MediaService.deleteFile(img.getUrl());
            }
        }

        // Xóa media audio của group
        if (group.getAudios() != null) {
            for (GroupQuestionAudio audio : group.getAudios()) {
                minIO_MediaService.deleteFile(audio.getUrl());
            }
        }

        // Xóa media cho các ToeicQuestion con trong group
        if (group.getQuestions() != null) {
            for (ToeicQuestion question : group.getQuestions()) {
                minIO_MediaService.deleteQuestionMedia(question);
            }
        }

        // Xóa group → cascade xóa các entity con trong DB
        groupRepo.delete(group);
    }

    public GroupQuestionResponseDTO getGroup(Integer id) {
        GroupQuestion gr = groupRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Group không tồn tại"));
        return toResponseDTO(gr);
    }

    private GroupQuestionResponseDTO toResponseDTO(GroupQuestion group) {
        GroupQuestionResponseDTO dto = new GroupQuestionResponseDTO();

        dto.setId(group.getId());
        dto.setPart(group.getPart());
        dto.setTitle(group.getTitle());
        dto.setContent(group.getContent());
        dto.setQuestionRange(group.getQuestionRange());
        dto.setExamId(group.getExam().getId());
        dto.setBankGroupId(group.getBankGroupId());
        dto.setIsContribute(group.getIsContribute());

        // ✅ Images - Presigned URLs để hiển thị
        dto.setImages(
                group.getImages().stream()
                        .map(img -> minIO_MediaService.getPresignedURL(img.getUrl(), Duration.ofDays(1)))
                        .toList());

        // ✅ Image Keys - để frontend gửi lại khi update
        dto.setImageKeys(
                group.getImages().stream()
                        .map(img -> img.getUrl())
                        .toList());

        // ✅ Audios - Presigned URLs để hiển thị
        dto.setAudios(
                group.getAudios().stream()
                        .map(audio -> minIO_MediaService.getPresignedURL(audio.getUrl(), Duration.ofDays(1)))
                        .toList());

        // ✅ Audio Keys - để frontend gửi lại khi update
        dto.setAudioKeys(
                group.getAudios().stream()
                        .map(audio -> audio.getUrl())
                        .toList());

        // ✅ Questions - Convert với presigned URLs và keys
        dto.setQuestions(
                group.getQuestions().stream()
                        .map(q -> new ToeicQuestionResponse(
                                q.getId(),
                                q.getIndexNumber(),
                                q.getPart(),
                                q.getDetail(),
                                q.getResult(),
                                // Images URLs
                                q.getImages().stream()
                                        .map(img -> minIO_MediaService.getPresignedURL(img.getUrl(),
                                                Duration.ofDays(1)))
                                        .toList(),
                                // Image Keys
                                q.getImages().stream()
                                        .map(img -> img.getUrl())
                                        .toList(),
                                // Audio URL
                                q.getAudio() != null && !q.getAudio().isEmpty()
                                        ? minIO_MediaService.getPresignedURL(q.getAudio(), Duration.ofDays(1))
                                        : null,
                                // Audio Key
                                q.getAudio(),
                                q.getConversation(),
                                q.getClarify(),
                                q.getIsContribute(),
                                q.getBankQuestionId(),
                                q.getOptions().stream()
                                        .map(o -> new ToeicQuestionResponse.OptionResponse(
                                                o.getMark(),
                                                o.getDetail()))
                                        .toList()))
                        .toList());
        return dto;
    }

    public ToeicQuestionResponse convertQuestionToResponse(ToeicQuestion question) {

        // Options
        List<ToeicQuestionResponse.OptionResponse> options = question.getOptions().stream()
                .map(opt -> new ToeicQuestionResponse.OptionResponse(
                        opt.getMark(),
                        opt.getDetail()))
                .collect(Collectors.toList());

        // Images URLs
        List<String> imageUrls = question.getImages() != null
                ? question.getImages().stream()
                        .map(img -> minIO_MediaService.getPresignedURL(img.getUrl(), Duration.ofMinutes(1)))
                        .collect(Collectors.toList())
                : List.of();

        // Image Keys
        List<String> imageKeys = question.getImages() != null
                ? question.getImages().stream()
                        .map(img -> img.getUrl())
                        .collect(Collectors.toList())
                : List.of();

        // Audio URL
        String audioUrl = question.getAudio() != null && !question.getAudio().isEmpty()
                ? minIO_MediaService.getPresignedURL(question.getAudio(), Duration.ofMinutes(1))
                : null;

        return new ToeicQuestionResponse(
                question.getId(),
                question.getIndexNumber(),
                question.getPart(),
                question.getDetail(),
                question.getResult(),
                imageUrls,
                imageKeys,
                audioUrl,
                question.getAudio(), // audioKey
                question.getConversation(),
                question.getClarify(),
                question.getIsContribute(),
                question.getBankQuestionId(),
                options);
    }

    @Transactional
    public ToeicQuestion updateQuestion(Integer questionId, ToeicQuestionForGroupRequestDTO request) {
        ToeicQuestion question = toeicQuestionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại"));
        GroupQuestion group = groupRepo.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group không tồn tại"));
        question.setGroup(group);
        return toeicQuestionRepository.save(question);
    }
}