package com.TestFlashCard.FlashCard.response;

import lombok.Data;

import java.util.List;

@Data
public class BankGroupQuestionResponse {

    private Long id;

    private String part;

    private String content;

    private Integer sourceGroupId;

    private List<String> images;

    // public url sau khi map từ audioKey
    private List<String> audios;

    private List<BankGroupChildQuestionResponse> questions;
}

