package com.TestFlashCard.FlashCard.request;

import lombok.Data;
import java.util.List;

@Data
public class ToeicQuestionRequestDTO {
    private Integer indexNumber;
    private String part;
    private String detail;
    private String result;
    private String clarify;

    private String audio;
    private String conversation;

    private Integer examId;
    private boolean random;
    private List<ToeicQuestionOptionRequestDTO> options;
    private List<ToeicQuestionImageRequestDTO> images;
}