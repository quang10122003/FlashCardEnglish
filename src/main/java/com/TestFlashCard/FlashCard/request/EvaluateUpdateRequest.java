package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EvaluateUpdateRequest {
    @NotNull
    private String adminReply;
}
