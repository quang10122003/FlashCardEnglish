package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ToeicCustomExamUpdateRequest {
    private Integer duration;
    @NotEmpty
    private String title;
    private Integer attemps;
}
