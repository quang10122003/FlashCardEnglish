package com.TestFlashCard.FlashCard.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BankToeicQuestionResponse {

    private Integer id;
    private String part;
    private String detail;
    private String result;
    private String clarify;

    private List<String> images;     // presigned URLs
    private List<String> imageKeys;  // keys

    private String audio;            // presigned URL
    private String audioKey;         // key

    private List<OptionResponse> options;

    @Data
    @AllArgsConstructor
    public static class OptionResponse {
        private String mark;
        private String detail;
    }
}

