package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CardCreateRequest {
    private String terminology;
    private String definition;
    private String audio;
    private String pronounce;
    private String partOfSpeech;
    private List<String> hint;
    private Integer level;
    private List<String> example;
    private String Image;

    
    @NotNull(message = "flashCard ID cannot be null")
    private Integer flashCardID;
}
