package com.TestFlashCard.FlashCard.request;

import com.TestFlashCard.FlashCard.Enum.FlashCardTopicStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FlashCardTopicCreateRequest {
    @NotNull(message = "Title cannot be null")
    @NotBlank(message = "Title cannot be blank")
    @NotEmpty(message = "Title cannot be empty")
    private String title;

    @NotNull(message = "Topic's status cannot be null")
    private FlashCardTopicStatus status;
}
