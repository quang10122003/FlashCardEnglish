package com.TestFlashCard.FlashCard.controller;

import com.TestFlashCard.FlashCard.exception.DuplicateGroupInBankException;
import com.TestFlashCard.FlashCard.exception.DuplicateQuestionInBankException;
import com.TestFlashCard.FlashCard.mapper.BankMapper;
import com.TestFlashCard.FlashCard.request.ListIdContributeRequest;
import com.TestFlashCard.FlashCard.request.UseFromBankRequest;
import com.TestFlashCard.FlashCard.response.*;
import com.TestFlashCard.FlashCard.service.QuestionBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question-bank")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService questionBankService;
    private final BankMapper bankMapper;

    // ==========================
    // ĐÓNG GÓP VÀO BANK
    // ==========================
    @PostMapping("/contribute/singleQuestion/bulk")
    public ApiResponse<List<BankToeicQuestionResponse>> contributeManyToeic(
            @RequestBody ListIdContributeRequest req
    ) {
        try {
            List<BankToeicQuestionResponse> res =
                    questionBankService.contributeManyToeicQuestions(req.getQuestionIds());

            return new ApiResponse<>(200, "Contributed successfully", res);

        } catch (DuplicateQuestionInBankException ex) {

            return new ApiResponse<>(
                    400,
                    "Some questions already exist in question bank",
                    ex.getDuplicatedResponses()
            );
        }
    }

    @PostMapping("/contribute/groupQuestion/bulk")
    public ApiResponse<List<?>> contributeManyGroup(
            @RequestBody ListIdContributeRequest req
    ) {
        try {
            return new ApiResponse<>(
                    200,
                    "Contributed group questions successfully",
                    questionBankService.contributeManyGroupQuestions(req.getQuestionIds())
            );
        } catch (DuplicateGroupInBankException ex) {
            return new ApiResponse<>(
                    400,
                    "Some groups already exist in question bank",
                    ex.getDuplicatedResponses()
            );
        }
    }

    // ==========================
    // DÙNG TỪ BANK VÀO EXAM
    // ==========================
    @PostMapping("/single")
    public ApiResponse<List<BankUseSingleQuestionResponse>> useSingle(
            @RequestBody UseFromBankRequest req
    ) {
        return ApiResponse.success(
                questionBankService.useSingleQuestions(
                        req.getIds().stream().map(Long::intValue).toList(), req.getExamId()
                )
        );
    }


    // ===== GROUP =====
    @PostMapping("/group")
    public ApiResponse<List<BankUseGroupQuestionResponse>> useGroup(
            @RequestBody UseFromBankRequest req
    ) {
        return ApiResponse.success(
                questionBankService.useGroupQuestions(req.getIds(), req.getExamId())
        );
    }
//    Get Detail
    @GetMapping("/single/{id}")
    public ApiResponse<BankToeicQuestionResponse> singleDetail(@PathVariable Integer id) {
        return ApiResponse.success(
                questionBankService.getSingleDetail(id)
        );
    }

    @GetMapping("/group/{id}")
    public ApiResponse<BankGroupQuestionResponse> groupDetail(@PathVariable Long id) {
        return ApiResponse.success(
                questionBankService.getGroupDetail(id)
        );
    }
    @GetMapping
    public ApiResponse<?> getAllQuestionFromBank(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam (required = true, defaultValue = "false") boolean isGroup,
            @RequestParam(defaultValue = "") String[] search) {
        try {
            return new ApiResponse<>( HttpStatus.CREATED.value(),"Lấy danh sách danh câu hỏi từ ngân hàng thành công",questionBankService.getAllQuestionFromBank(pageNo,pageSize,sortBy,isGroup,search)); // Mã 201
        } catch (Exception e) {
            // Xử lý lỗi nếu có (ví dụ: tên danh mục bị trùng unique constraint)
            return new ApiResponse<>( HttpStatus.INTERNAL_SERVER_ERROR.value(),"Lấy danh sách câu hỏi từ ngân hàng thất bại vì: " +e.getMessage());
        }
    }
}

