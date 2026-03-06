package com.TestFlashCard.FlashCard.request;

import com.TestFlashCard.FlashCard.Enum.FlashCardTopicStatus;
import com.TestFlashCard.FlashCard.Enum.LearningStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FlashCardTopicUpdateRequest {
    @NotNull(message = "Topic's id cannot be null")
    private Integer id;

    @NotNull(message = "Topic's title cannot be null")
    @NotBlank(message = "Topic's title cannot be blank")
    @NotEmpty(message = "Topic's title cannot be empty")
    private String title;
    private FlashCardTopicStatus status;
    private LearningStatus learningStatus;
    private Integer visitCount;
}
