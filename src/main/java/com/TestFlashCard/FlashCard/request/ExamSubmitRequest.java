package com.TestFlashCard.FlashCard.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExamSubmitRequest {
    @NotNull
    private Integer examID;
    private String selectedPart;
    private Integer duration;
    private List<ToeicQuestionRecord> answers;
}
