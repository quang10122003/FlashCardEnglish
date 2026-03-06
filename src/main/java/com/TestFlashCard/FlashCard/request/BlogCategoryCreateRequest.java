package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlogCategoryCreateRequest {
    @NotBlank
    @NotNull
    @NotEmpty
    private String title;
}
