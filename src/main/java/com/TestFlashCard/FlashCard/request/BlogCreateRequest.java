package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlogCreateRequest {
    @NotNull
    @NotEmpty
    @NotBlank
    private String title;
    @NotNull
    @NotEmpty
    @NotBlank
    private String category;
    @NotNull
    @NotEmpty
    @NotBlank
    private String shortDetail;
    @NotNull
    @NotEmpty
    @NotBlank
    private String detail;

    private String author;
}
