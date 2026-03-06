package com.TestFlashCard.FlashCard.exception;

import com.TestFlashCard.FlashCard.entity.BankToeicQuestion;
import com.TestFlashCard.FlashCard.response.BankToeicQuestionResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class DuplicateQuestionInBankException extends RuntimeException {

    private final List<BankToeicQuestionResponse> duplicatedResponses;

    public DuplicateQuestionInBankException(List<BankToeicQuestionResponse> duplicatedResponses) {
        super("Duplicate questions in bank");
        this.duplicatedResponses = duplicatedResponses;
    }

//    public List<BankToeicQuestionResponse> getDuplicatedResponses() {
//        return duplicatedResponses;
//    }
}


