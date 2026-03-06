package com.TestFlashCard.FlashCard.controller;

import com.TestFlashCard.FlashCard.request.DisorderExamUpdateRequest;
import com.TestFlashCard.FlashCard.request.DisorderQuestionRequest;
import com.TestFlashCard.FlashCard.request.DisorderGroupRequest;
import com.TestFlashCard.FlashCard.request.DisorderReorderRequest;
import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.TestFlashCard.FlashCard.response.DisorderExamResponse;
import com.TestFlashCard.FlashCard.service.DisorderExamService;
import com.TestFlashCard.FlashCard.service.UserService;
import com.TestFlashCard.FlashCard.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/disorder-exam")
@RequiredArgsConstructor
public class DisorderExamController {

    private final DisorderExamService disorderExamService;
    private final UserService userService;

    // ==================== EXAM CRUD ====================

    @PostMapping("/create")
    public ApiResponse<?> createExam(Principal principal) {
        try {
            User user = userService.getUserByAccountName(principal.getName());
            DisorderExamResponse response = disorderExamService.createDraft(user);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Tạo đề thi ngẫu nhiên thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Tạo đề thất bại: " + e.getMessage());
        }
    }

    @GetMapping("/{examId}")
    public ApiResponse<?> getExamDetail(@PathVariable Integer examId, Principal principal) {
        try {
            User user = userService.getUserByAccountName(principal.getName());
            DisorderExamResponse response = disorderExamService.getDetail(examId, user);
            return new ApiResponse<>(HttpStatus.OK.value(), "Lấy chi tiết đề thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Lấy đề thất bại: " + e.getMessage());
        }
    }

    @PutMapping("/{examId}")
    public ApiResponse<?> updateExam(
            @PathVariable Integer examId,
            @RequestBody DisorderExamUpdateRequest request,
            Principal principal) {
        try {
            User user = userService.getUserByAccountName(principal.getName());
            DisorderExamResponse response = disorderExamService.updateExam(examId, user, request);
            return new ApiResponse<>(HttpStatus.OK.value(), "Cập nhật đề thành công", response);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Cập nhật thất bại: " + e.getMessage());
        }
    }

    @DeleteMapping("/{examId}")
    public ApiResponse<?> deleteExam(@PathVariable Integer examId, Principal principal) {
        try {
            User user = userService.getUserByAccountName(principal.getName());
            disorderExamService.deleteExam(examId, user);
            return new ApiResponse<>(HttpStatus.OK.value(), "Xóa đề thành công");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Xóa đề thất bại: " + e.getMessage());
        }
    }

    @GetMapping("/my-exams")
    public ApiResponse<?> getMyExams(Principal principal) {
        try {
            User user = userService.getUserByAccountName(principal.getName());
            return new ApiResponse<>(HttpStatus.OK.value(), "Lấy danh sách thành công",
                    disorderExamService.getMyDisorderExams(user));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Lấy danh sách thất bại: " + e.getMessage());
        }
    }

    // ==================== SINGLE QUESTION ====================

    @PostMapping("/{examId}/question")
    public ApiResponse<?> addQuestion(
            @PathVariable Integer examId,
            @RequestBody DisorderQuestionRequest request) {
        try {
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Thêm câu hỏi thành công",
                    disorderExamService.addQuestion(examId, request));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Thêm câu hỏi thất bại: " + e.getMessage());
        }
    }

    @PutMapping("/{examId}/question/{questionId}")
    public ApiResponse<?> updateQuestion(
            @PathVariable Integer examId,
            @PathVariable Integer questionId,
            @RequestBody DisorderQuestionRequest request) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Cập nhật câu hỏi thành công",
                    disorderExamService.updateQuestion(examId, questionId, request));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Cập nhật thất bại: " + e.getMessage());
        }
    }

    @DeleteMapping("/{examId}/question/{questionId}")
    public ApiResponse<?> deleteQuestion(
            @PathVariable Integer examId,
            @PathVariable Integer questionId) {
        try {
            disorderExamService.deleteQuestion(examId, questionId);
            return new ApiResponse<>(HttpStatus.OK.value(), "Xóa câu hỏi thành công");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Xóa câu hỏi thất bại: " + e.getMessage());
        }
    }

    // ==================== GROUP QUESTION ====================

    @PostMapping("/{examId}/group")
    public ApiResponse<?> addGroup(
            @PathVariable Integer examId,
            @RequestBody DisorderGroupRequest request) {
        try {
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Thêm nhóm thành công",
                    disorderExamService.addGroup(examId, request));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Thêm nhóm thất bại: " + e.getMessage());
        }
    }

    @PutMapping("/{examId}/group/{groupId}")
    public ApiResponse<?> updateGroup(
            @PathVariable Integer examId,
            @PathVariable Integer groupId,
            @RequestBody DisorderGroupRequest request) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Cập nhật nhóm thành công",
                    disorderExamService.updateGroup(examId, groupId, request));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Cập nhật nhóm thất bại: " + e.getMessage());
        }
    }

    @DeleteMapping("/{examId}/group/{groupId}")
    public ApiResponse<?> deleteGroup(
            @PathVariable Integer examId,
            @PathVariable Integer groupId) {
        try {
            disorderExamService.deleteGroup(examId, groupId);
            return new ApiResponse<>(HttpStatus.OK.value(), "Xóa nhóm thành công");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Xóa nhóm thất bại: " + e.getMessage());
        }
    }

    @PostMapping("/{examId}/group/{groupId}/question")
    public ApiResponse<?> addQuestionToGroup(
            @PathVariable Integer examId,
            @PathVariable Integer groupId,
            @RequestBody DisorderQuestionRequest request) {
        try {
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Thêm câu hỏi vào nhóm thành công",
                    disorderExamService.addQuestionToGroup(examId, groupId, request));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Thêm câu hỏi thất bại: " + e.getMessage());
        }
    }

    // ==================== REORDER ====================

    @PutMapping("/{examId}/reorder")
    public ApiResponse<?> reorderItems(
            @PathVariable Integer examId,
            @RequestBody DisorderReorderRequest request) {
        try {
            disorderExamService.reorderItems(examId, request);
            return new ApiResponse<>(HttpStatus.OK.value(), "Sắp xếp lại thành công");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Sắp xếp thất bại: " + e.getMessage());
        }
    }
}