package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FlashCardCreateRequest {
    @NotNull(message = "Title cannot be null")
    @NotBlank(message = "Title cannot be blank")
    @NotEmpty(message = "Title cannot be empty")
    private String title;

    @NotNull(message = "Topic ID cannot be null")
    private Integer topicID;

    @Min(value = 1, message = "Cycle must be greater than or equal to 1")
    @Max(value = 30, message = "Cycle must be smaller than or equal to 30")
    private Integer cycle;
}
