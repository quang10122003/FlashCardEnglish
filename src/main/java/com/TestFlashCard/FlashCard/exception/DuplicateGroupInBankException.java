package com.TestFlashCard.FlashCard.exception;

import com.TestFlashCard.FlashCard.response.BankGroupQuestionResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class DuplicateGroupInBankException extends RuntimeException {

    private final List<BankGroupQuestionResponse> duplicatedResponses;

    public DuplicateGroupInBankException(List<BankGroupQuestionResponse> data) {
        super("Group questions already exist in bank");
        this.duplicatedResponses = data;
    }
}

