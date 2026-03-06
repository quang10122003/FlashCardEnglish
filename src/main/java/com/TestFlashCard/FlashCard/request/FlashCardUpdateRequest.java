package com.TestFlashCard.FlashCard.request;

import java.time.LocalDate;

import com.TestFlashCard.FlashCard.Enum.LearningStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FlashCardUpdateRequest {
    @NotNull(message = "FlashCard's id cannot be null")
    private Integer id;

    @Size(min = 1, max = 100, message = "Title's size must be between 1 and 100")
    private String title;

    private LearningStatus learningStatus;
    private LocalDate reviewDate;

    @Min(value = 1, message = "Cycle must be greater than or equal to 1")
    @Max(value = 30, message = "Cycle must be smaller than or equal to 30")
    private Integer cycle;
}
