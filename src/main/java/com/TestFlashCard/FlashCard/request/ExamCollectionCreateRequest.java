package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExamCollectionCreateRequest {
    @NotNull
    private int id;
    @NotNull
    @NotBlank
    @NotEmpty
    private String collection;
}
