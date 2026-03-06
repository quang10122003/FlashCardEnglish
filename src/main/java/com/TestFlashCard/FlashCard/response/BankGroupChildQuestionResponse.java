package com.TestFlashCard.FlashCard.response;

import lombok.Data;

import java.util.List;

@Data
public class BankGroupChildQuestionResponse {

    private Long id;

    private Integer indexNumber;

    private String detail;

    private String result;

    private String clarify;

    private List<BankToeicOptionResponse> options;
}
