package com.TestFlashCard.FlashCard.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ToeicQuestionResponse {

    private Integer id;
    private Integer indexNumber;
    private String part;
    private String detail;
    private String result;

    private List<String> images;
    private List<String> imageKeys;

    private String audio;
    private String audioKey;
    private String conversation;
    private String clarify;
    private Boolean isContribute;
    private Integer bankQuestionId;
    private List<OptionResponse> options;

    @Data
    @AllArgsConstructor
    public static class OptionResponse {
        private String mark;
        private String detail;
    }
}
