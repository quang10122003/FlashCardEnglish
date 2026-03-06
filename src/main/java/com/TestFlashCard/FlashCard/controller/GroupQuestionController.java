package com.TestFlashCard.FlashCard.controller;

import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import com.TestFlashCard.FlashCard.request.GroupQuestionRequestDTO;
import com.TestFlashCard.FlashCard.request.ToeicQuestionForGroupRequestDTO;
import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.TestFlashCard.FlashCard.response.ToeicQuestionResponse;
import com.TestFlashCard.FlashCard.service.GroupQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/group-question")
@RequiredArgsConstructor
public class GroupQuestionController {

    private final GroupQuestionService service;

    @PostMapping
    public ApiResponse<?> createGroup(@RequestBody GroupQuestionRequestDTO req) {
        try {
            return new ApiResponse<>(200, "Tạo group thành công", service.createGroup(req));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Tạo group thất bại vì " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateGroup(
            @PathVariable Integer id,
            @RequestBody GroupQuestionRequestDTO req) {

        try {
            return new ApiResponse<>(200, "Cập nhật group thành công", service.updateGroup(id, req));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Cập nhật group thất bại vì " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteGroup(@PathVariable Integer id) {
        try {
            service.deleteGroup(id);
            return new ApiResponse<>(200, "Xóa group thành công", null);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Xóa group thất bại vì " + e.getMessage());
        }

    }

    @GetMapping("/{id}")
    public ApiResponse<?> getGroup(@PathVariable Integer id) {
        try {
            return new ApiResponse<>(200, "Lấy group thành công", service.getGroup(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Lấy group thất bại vì " + e.getMessage());
        }
    }

    @PutMapping("question/{questionId}")
    public ApiResponse<?> updateQuestionForGroup(@PathVariable Integer questionId,
            @RequestBody ToeicQuestionForGroupRequestDTO request) {
        try {
            ToeicQuestion updated = service.updateQuestion(questionId, request);
            ToeicQuestionResponse response = service.convertQuestionToResponse(updated);
            return new ApiResponse<>(HttpStatus.OK.value(), "Cập nhật câu hỏi thành công", response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Cập nhật thất bại: " + e.getMessage());
        }
    }
}
