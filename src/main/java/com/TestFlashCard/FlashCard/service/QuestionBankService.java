package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.response.*;

import java.util.List;

public interface QuestionBankService {
    List<BankToeicQuestionResponse> contributeManyToeicQuestions(List<Integer> ids);
    List<BankGroupQuestionResponse> contributeManyGroupQuestions(List<Integer> ids);
    List<BankUseSingleQuestionResponse> useSingleQuestions(List<Integer> ids, int examId);
    List<BankUseGroupQuestionResponse> useGroupQuestions(List<Long> ids,int examId);

    BankToeicQuestionResponse getSingleDetail(Integer id);

    BankGroupQuestionResponse getGroupDetail(Long id);

    PageResponse<?> getAllQuestionFromBank(int pageNo, int pageSize, String sortBy, boolean isGroup, String[] search);
}
