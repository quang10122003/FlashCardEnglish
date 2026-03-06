package com.TestFlashCard.FlashCard.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.TestFlashCard.FlashCard.entity.*;
import com.TestFlashCard.FlashCard.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import com.TestFlashCard.FlashCard.JpaSpec.ExamSpecification;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.CustomExamRepository;
import com.TestFlashCard.FlashCard.repository.ICommentReply_Repository;
import com.TestFlashCard.FlashCard.repository.IComment_Repository;
import com.TestFlashCard.FlashCard.repository.IExam_Repository;
import com.TestFlashCard.FlashCard.repository.IToeicQuestion_Repository;
import com.TestFlashCard.FlashCard.request.ExamCreateRequest;
import com.TestFlashCard.FlashCard.request.ExamCreateRequestVer2;
import com.TestFlashCard.FlashCard.request.ExamUpdateRequest;
import com.TestFlashCard.FlashCard.request.ToeicCustomExamUpdateRequest;

import org.springframework.util.FileSystemUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamService {
    @Autowired
    private final IExam_Repository exam_Repository;
    @Autowired
    private final IToeicQuestion_Repository toeicQuestion_repository;
    @Autowired
    private final ExcelParser excelParser;
    @Autowired
    private MinIO_MediaService minIO_MediaService;
    @Autowired
    private final IComment_Repository comment_Repository;
    @Autowired
    private final ICommentReply_Repository commentReply_Repository;
    @Autowired
    private final ExamTypeService examTypeService;
    @Autowired
    private final ExamCollectionService examCollectionService;
    @Autowired
    private CustomExamRepository customExamRepository;

    public List<ExamInformationResponse> getByFilter(Integer year, String type, String collection, String title) {
        Specification<Exam> spec = Specification.where(ExamSpecification.hasYear(year))
                .and(ExamSpecification.hasType(type)).and(ExamSpecification.hasCollection(collection))
                .and(ExamSpecification.containsTitle(title));
        return exam_Repository.findAll(spec).stream().map(this::convertToExamDetailResponse).toList();
    }

    public List<ExamInformationResponse> getUserExamsByFilter(
            Integer year, String type, String collection, String title) {

        Specification<Exam> spec = Specification
                .where(ExamSpecification.isUserExam())
                .and(ExamSpecification.hasYear(year))
                .and(ExamSpecification.hasType(type))
                .and(ExamSpecification.hasCollection(collection))
                .and(ExamSpecification.containsTitle(title));
        return exam_Repository.findAll(spec)
                .stream()
                .map(this::convertToExamDetailResponse)
                .toList();
    }

    public ExamFilterdResponse convertToResponse(Exam exam) {
        return new ExamFilterdResponse(
                exam.getId(),
                exam.getTitle());
    }

    public ExamInformationResponse getByID(int examID) throws IOException {
        Exam exam = exam_Repository.findById(examID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Exam with id : " + examID));
        return convertToExamDetailResponse(exam);
    }

    public int countAllCommentsAndReplies(Integer examId) {
        int commentCount = comment_Repository.countByExamId(examId);
        int replyCount = commentReply_Repository.countRepliesByExamId(examId);
        return commentCount + replyCount;
    }

    public List<ExamInformationResponse> getByCreatAt() {
        List<Exam> exams = exam_Repository.findAllByOrderByCreatedAtDesc();
        List<CustomExam> customExams = customExamRepository.findAll();

        // Lấy danh sách examId đã có trong custom_exam
        Set<Integer> customExamIds = customExams.stream()
                .map(customExam -> customExam.getCustomExam().getId())
                .collect(Collectors.toSet());

        return exams.stream()
                .filter(exam -> !customExamIds.contains(exam.getId()))
                .map(this::convertToExamDetailResponse)
                .toList();
    }

    public ExamInformationResponse convertToExamDetailResponse(Exam exam) {
        List<ToeicQuestionResponse> singleQuestions = exam.getQuestions().stream()
                .filter(q -> !List.of("3", "4", "6", "7").contains(q.getPart())) // Thêm "6"
                .map(this::convertQuestionToResponse)
                .toList();

        List<GroupQuestionResponseDTO> groupQuestions = exam.getGroupQuestions().stream()
                .map(this::convertGroupToResponse)
                .toList();
        return new ExamInformationResponse(
                exam.getId(),
                exam.getDuration() != null ? exam.getDuration() : null, // giữ null nếu không có giá trị
                getNumOfPart(exam.getId()),
                getNumOfQuestion(exam.getId()),
                exam.getTitle(),
                exam.getYear() != null ? exam.getYear() : null,
                exam.getType() != null ? exam.getType().getType() : null,
                exam.getCollection() != null ? exam.getCollection().getCollection() : null,
                exam.isRandom(),
                exam.getAttemps(),
                countAllCommentsAndReplies(exam.getId()),
                exam.getFileImportName(),
                singleQuestions,
                groupQuestions);
    }

    public GroupQuestionResponseDTO convertGroupToResponse(GroupQuestion group) {
        List<String> imageKeys = group.getImages().stream()
                .map(img -> img.getUrl())
                .toList();

        List<String> imageUrls = group.getImages().stream()
                .map(img -> minIO_MediaService.getPresignedURL(img.getUrl(), Duration.ofDays(1)))
                .toList();

        List<String> audioKeys = group.getAudios().stream()
                .map(a -> a.getUrl())
                .toList();

        List<String> audioUrls = group.getAudios().stream()
                .map(a -> minIO_MediaService.getPresignedURL(a.getUrl(), Duration.ofDays(1)))
                .toList();
        List<ToeicQuestionResponse> childQuestions = group.getQuestions() != null
                ? group.getQuestions().stream()
                        .map(this::convertQuestionToResponse)
                        .toList()
                : List.of();

        return new GroupQuestionResponseDTO(
                group.getId(),
                group.getPart(),
                group.getTitle(),
                group.getContent(),
                group.getQuestionRange(),
                group.getExam().getId(),
                group.getIsContribute(),
                group.getBankGroupId(),
                imageUrls, // URLs
                imageKeys, // KEYS
                audioUrls, // URLs
                audioKeys, // KEYS
                childQuestions);

    }

    public ToeicQuestionResponse convertQuestionToResponse(ToeicQuestion question) {

        // Options
        List<ToeicQuestionResponse.OptionResponse> options = question.getOptions().stream()
                .map(opt -> new ToeicQuestionResponse.OptionResponse(opt.getMark(), opt.getDetail()))
                .collect(Collectors.toList());

        // Images (new)
        List<String> imageUrls = question.getImages() != null
                ? question.getImages().stream()
                        .map(i -> minIO_MediaService.getPresignedURL(i.getUrl(), Duration.ofMinutes(1)))
                        .collect(Collectors.toList())
                : List.of();

        // Audio (unchanged)
        String audio = null;
        if (question.getAudio() != null && !question.getAudio().isEmpty()) {
            audio = minIO_MediaService.getPresignedURL(question.getAudio(), Duration.ofDays(1));
        }

        return new ToeicQuestionResponse(
                question.getId(),
                question.getIndexNumber(),
                question.getPart(),
                question.getDetail(),
                question.getResult(),
                imageUrls, // <-- LIST mớ
                question.getImages().stream().map(img -> img.getUrl()).toList(),
                audio,
                question.getAudio(),
                question.getConversation(),
                question.getClarify(),
                question.getIsContribute(),
                question.getBankQuestionId(),
                options);
    }

    @Transactional
    public ExamFilterdResponse create(ExamCreateRequest examDetail) throws IOException {
        Exam exam = new Exam();

        ExamType examType = examTypeService.getDetailByType(examDetail.getType());
        ExamCollection examCollection = examCollectionService.getDetailByCollection(examDetail.getCollection());

        System.out.println(examCollection);

        exam.setCollection(examCollection);
        exam.setDuration(examDetail.getDuration());
        exam.setTitle(examDetail.getTitle());
        exam.setType(examType);
        exam.setYear(examDetail.getYear());
        exam.setRandom(examDetail.getIsRandom());
        exam.setAttemps(0);
        exam_Repository.save(exam);
        return convertToResponse(exam);
    }

    @Transactional
    public ExamFilterdResponse create(ExamCreateRequestVer2 examDetail) throws IOException {
        Exam exam = new Exam();
        exam.setTitle(examDetail.getTitle());
        exam.setAttemps(0);
        exam_Repository.save(exam);
        return convertToResponse(exam);
    }

    public int getNumOfQuestion(int examID) {
        return toeicQuestion_repository.countQuestionsByExamId(examID);
    }

    public int getNumOfPart(int examID) {
        // return default number : 7
        return 7;
    }

    @Transactional
    public void updateExam(ExamUpdateRequest examDetail, int examID) throws IOException {
        Exam exam = exam_Repository.findById(examID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Exam with id : " + examID));

        if (examDetail.getDuration() != null)
            exam.setDuration(examDetail.getDuration());
        if (examDetail.getCollection() != null) {
            ExamCollection examCollection = examCollectionService.getDetailByCollection(examDetail.getCollection());
            exam.setCollection(examCollection);
        }
        if (examDetail.getTitle() != null)
            exam.setTitle(examDetail.getTitle());
        if (examDetail.getType() != null) {
            ExamType examType = examTypeService.getDetailByType(examDetail.getType());
            exam.setType(examType);
        }
        if (examDetail.getYear() != null)
            exam.setYear(examDetail.getYear());
        if (examDetail.getAttemps() != null)
            exam.setAttemps(examDetail.getAttemps());

        exam_Repository.save(exam);
    }

    @Transactional
    public void deleteById(int examID) {

        customExamRepository.deleteByCustomExam_Id(examID);
        Exam exam = exam_Repository.findById(examID)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find Exam with id: " + examID));

        // Xóa media các ToeicQuestion trực tiếp
        for (ToeicQuestion question : exam.getQuestions()) {
            minIO_MediaService.deleteQuestionMedia(question);
        }

        // Xóa media các GroupQuestion
        if (exam.getGroupQuestions() != null) {
            for (GroupQuestion group : exam.getGroupQuestions()) {
                // Xóa ảnh của group
                if (group.getImages() != null) {
                    for (GroupQuestionImage img : group.getImages()) {
                        minIO_MediaService.deleteFile(img.getUrl());
                    }
                }
                // Xóa audio của group
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
            }
        }

        // Xóa Exam → cascade sẽ xóa tất cả entity
        exam_Repository.delete(exam);
    }

    @Transactional
    public void importQuestions(MultipartFile zipFile, Integer examId) throws IOException {
        // Lấy exam đã tồn tại
        Exam exam = exam_Repository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        exam.setFileImportName(zipFile.getOriginalFilename());

        // Xóa dữ liệu cũ
        // 1. Xóa media của single questions
        if (exam.getQuestions() != null) {
            for (ToeicQuestion question : exam.getQuestions()) {
                minIO_MediaService.deleteQuestionMedia(question);
            }
            exam.getQuestions().clear();
        }

        // 2. Xóa media của group questions
        if (exam.getGroupQuestions() != null) {
            for (GroupQuestion group : exam.getGroupQuestions()) {
                // Xóa ảnh của group
                if (group.getImages() != null) {
                    for (GroupQuestionImage img : group.getImages()) {
                        minIO_MediaService.deleteFile(img.getUrl());
                    }
                }
                // Xóa audio của group
                if (group.getAudios() != null) {
                    for (GroupQuestionAudio audio : group.getAudios()) {
                        minIO_MediaService.deleteFile(audio.getUrl());
                    }
                }
                // Xóa media của các câu hỏi con trong group
                if (group.getQuestions() != null) {
                    for (ToeicQuestion question : group.getQuestions()) {
                        minIO_MediaService.deleteQuestionMedia(question);
                    }
                }
            }
            exam.getGroupQuestions().clear();
        }

        // Extract file zip
        Path tempDir = Files.createTempDirectory("uploadExam");
        File file = new File(tempDir.toFile(), zipFile.getOriginalFilename());
        zipFile.transferTo(file);
        ZipUtil.unpack(file, tempDir.toFile());

        File excelFile = new File(tempDir.toFile(), "questions.xlsx");
        File mediaDir = new File(tempDir.toFile(), "media");

        // Parse với method mới - trả về cả single và group questions
        ExcelParser.ParseResult parseResult = excelParser.parseQuestionsWithGroups(excelFile, mediaDir, exam);

        // Thêm single questions (Part 1, 2, 5)
        for (ToeicQuestion q : parseResult.getSingleQuestions()) {
            q.setExam(exam);
            exam.getQuestions().add(q);
        }

        // Thêm group questions (Part 3, 4, 6, 7)
        for (GroupQuestion group : parseResult.getGroupQuestions()) {
            group.setExam(exam);
            // Đảm bảo các câu hỏi con cũng được liên kết đúng
            if (group.getQuestions() != null) {
                for (ToeicQuestion q : group.getQuestions()) {
                    q.setExam(exam);
                    q.setGroup(group);
                }
            }
            exam.getGroupQuestions().add(group);
        }

        exam_Repository.save(exam);
        FileSystemUtils.deleteRecursively(tempDir);
    }

    public long countAll() {
        return exam_Repository.count();
    }

    public List<ExamAttemp> getTop3ExamByAttemps() {
        List<Exam> exams = exam_Repository.findTop3ByOrderByAttempsDesc();
        return exams.stream()
                .map(e -> {
                    ExamAttemp dto = new ExamAttemp();
                    dto.setTest(e.getTitle());
                    dto.setAttemps(e.getAttemps());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ToeicCustomUpdateResponse updateToeicCustomExam(ToeicCustomExamUpdateRequest request, int examId) {
        Exam exam = exam_Repository.findById(examId).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Toeic Custom Exam with id : " + examId));

        if (request.getTitle() != null)
            exam.setTitle(request.getTitle());
        if (request.getDuration() != null)
            exam.setDuration(request.getDuration());
        if (request.getAttemps() != null)
            exam.setAttemps(request.getAttemps());
        exam_Repository.save(exam);
        ToeicCustomUpdateResponse response = new ToeicCustomUpdateResponse();
        response.setAttemps(exam.getAttemps());
        response.setDuration(exam.getDuration());
        response.setTitle(exam.getTitle());

        return response;
    }

    public List<ExamInformationResponse> getAllCustomExam(int userId) {
        return customExamRepository.findByUserId(userId)
                .stream()
                .map(CustomExam::getCustomExam)
                .map(this::convertToExamDetailResponse)
                .toList();
    }

    public List<CustomExamResponse> getCustomExams(User user) {
        List<CustomExam> customExams = customExamRepository.findByUserId(user.getId());

        return customExams.stream()
                .map(ce -> {
                    Exam exam = ce.getCustomExam();

                    // Đếm số câu hỏi (optional)
                    int totalQuestions = toeicQuestion_repository.countByExam_Id(exam.getId());

                    return new CustomExamResponse(
                            exam.getId(),
                            exam.getTitle(),
                            exam.getDuration(),
                            exam.isRandom(), // <-- Trả về isRandom
                            exam.getYear(),
                            exam.getCreatedAt(),
                            totalQuestions);
                })
                .toList();
    }

    @Transactional
    public ExamFilterdResponse createCustomDraft(ExamCreateRequestVer2 request, User user) throws IOException {
        ExamType type = examTypeService.getDetailByType("CUSTOM");
        // 1. Tạo exam như bình thường
        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setType(type);
        exam.setDuration(request.getDuration());
        exam.setAttemps(0);
        exam.setRandom(false);
        exam_Repository.save(exam);

        // 2. Gắn exam với user qua bảng custom_exam
        CustomExam customExam = new CustomExam();
        customExam.setUser(user);
        customExam.setCustomExam(exam); // giữ đúng field hiện tại của bạn
        customExamRepository.save(customExam);

        return convertToResponse(exam);
    }

    @Transactional
    public void deleteCustomExam(int examId, User user) {

        CustomExam customExam = customExamRepository
                .findByUserIdAndCustomExamId(user.getId(), examId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Custom exam not found or not belong to user"));

        // Xóa liên kết custom
        customExamRepository.delete(customExam);

        // Xóa exam (nếu custom exam là exam riêng)
        exam_Repository.delete(customExam.getCustomExam());
    }

    @Transactional
    public ToeicCustomUpdateResponse updateCustomExam(
            ToeicCustomExamUpdateRequest request,
            int examId,
            User user) {

        // Check quyền sở hữu
        CustomExam customExam = customExamRepository
                .findByUserIdAndCustomExamId(user.getId(), examId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Custom exam not found or not belong to user"));

        // Dùng lại logic cũ
        return updateToeicCustomExam(request, examId);
    }
    // ExamService.java

    // Lấy đề hệ thống (có filter)

    public List<ExamInformationResponse> getSystemExamsByFilter(
            Integer year, String type, String collection, String title) {

        Specification<Exam> spec = Specification
                .where(ExamSpecification.isSystemExam())
                .and(ExamSpecification.isNotRandom()) // ✅ THÊM: Chỉ lấy format TOEIC
                .and(ExamSpecification.isNotDeleted()) // ✅ THÊM: Không lấy đề đã xóa
                .and(ExamSpecification.hasYear(year))
                .and(ExamSpecification.hasType(type))
                .and(ExamSpecification.hasCollection(collection))
                .and(ExamSpecification.containsTitle(title));

        return exam_Repository.findAll(spec)
                .stream()
                .map(this::convertToExamDetailResponse)
                .toList();
    }

    // Lấy đề hệ thống (không filter) - sắp xếp theo createdAt

    public List<ExamInformationResponse> getSystemExamsByCreatedAt() {
        List<Integer> customExamIds = customExamRepository.findAll().stream()
                .map(ce -> ce.getCustomExam().getId())
                .toList();

        return exam_Repository.findAllByOrderByCreatedAtDesc().stream()
                .filter(exam -> !exam.isRandom()) // ✅ CHỈ LẤY FORMAT TOEIC
                .filter(exam -> !exam.isDeleted()) // ✅ KHÔNG LẤY ĐỀ ĐÃ XÓA
                .filter(exam -> !customExamIds.contains(exam.getId()))
                .map(this::convertToExamDetailResponse)
                .toList();
    }
}
