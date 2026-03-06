package com.TestFlashCard.FlashCard.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BankUseGroupQuestionResponse {

    private Long id;
    private String part;
    private String content;

    private List<MediaFileResponse> images;
    private List<MediaFileResponse> audios;

    private List<ChildQuestion> questions;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ChildQuestion {
        private Long id;
        private Integer indexNumber;
        private String detail;
        private String result;
        private String clarify;
        private List<BankToeicOptionResponse> options;
    }
}

