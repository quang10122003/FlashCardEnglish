package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExamTypeCreateRequest {
    @NotNull
    private String type;
}
