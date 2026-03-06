package com.TestFlashCard.FlashCard.controller;

import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import com.TestFlashCard.FlashCard.request.ToeicQuestionReorderRequest;
import com.TestFlashCard.FlashCard.request.ToeicQuestionRequestDTO;
import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.TestFlashCard.FlashCard.response.ToeicQuestionResponse;
import com.TestFlashCard.FlashCard.service.ToeicQuestionService;
import lombok.RequiredArgsConstructor;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/toeic-question")
@RequiredArgsConstructor
public class ToeicQuestionController {

    private final ToeicQuestionService toeicQuestionService;

    @GetMapping("/{id}")
    public ApiResponse<?> getToeicQuestion(@PathVariable Integer id) {
        return new ApiResponse<>(HttpStatus.OK.value(),"Get toeic question detail success!", toeicQuestionService.getById(id));
    }
    

    @PostMapping
    public ApiResponse<?> create(@RequestBody ToeicQuestionRequestDTO request) {
        try {
            ToeicQuestion created = toeicQuestionService.createToeicQuestion(request);

            // Convert sang Response DTO
            ToeicQuestionResponse response = toeicQuestionService.convertQuestionToResponse(created);

            return new ApiResponse<>(HttpStatus.CREATED.value(), "Tạo thành công câu hỏi", response);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(),
                    "Tạo câu hỏi thất bại vì: " + e.getMessage());
        }
    }

    @DeleteMapping("/{questionId}")
    public ApiResponse<?> deleteQuestion(@PathVariable Integer questionId) {
        try {
            toeicQuestionService.deleteToeicQuestion(questionId);
            return new ApiResponse<>(HttpStatus.OK.value(), "Xoá câu hỏi thành công");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Xoá câu hỏi thất bại: " + e.getMessage());
        }
    }

    @PutMapping("/{questionId}")
    public ApiResponse<?> updateQuestion(@PathVariable Integer questionId,
            @RequestBody ToeicQuestionRequestDTO request) {
        try {
            ToeicQuestion updated = toeicQuestionService.updateQuestion(questionId, request);
            ToeicQuestionResponse response = toeicQuestionService.convertQuestionToResponse(updated);
            return new ApiResponse<>(HttpStatus.OK.value(), "Cập nhật câu hỏi thành công", response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Cập nhật thất bại: " + e.getMessage());
        }
    }

    @PutMapping("reorder/{examId}")
    public ApiResponse<?> reorderQuestionsIndexNumber(@PathVariable Integer examId, @RequestBody ToeicQuestionReorderRequest request) throws BadRequestException{
        
        toeicQuestionService.reorder(request, examId);
        
        return new ApiResponse<>(HttpStatus.OK.value(), "Thay doi thu tu thanh cong");
    }
}
