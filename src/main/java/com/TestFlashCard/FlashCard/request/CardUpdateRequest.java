package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardUpdateRequest {
    private String terminology;
    private String definition;
    private String audio;
    private String pronounce;
    private String partOfSpeech;
    private Integer level;
    private String example;
    private Integer isRemember;
    private String hint;
    
    @NotNull(message = "flashCard ID cannot be null")
    private Integer flashCardID;
}
