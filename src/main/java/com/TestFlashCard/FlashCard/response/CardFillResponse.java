package com.TestFlashCard.FlashCard.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardFillResponse {
    private String pronounce;
    private String audio;
    private String partOfSpeech;
    private List<String> example;
    private List<String> hint;
    private String definition;
}
