package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EvaluateCreateRequest {
    private String content;
    @NotNull
    private Integer star;
}
