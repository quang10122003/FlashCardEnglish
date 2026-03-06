package com.TestFlashCard.FlashCard.response;

import lombok.Getter;

import java.util.List;


public record CardsResponse(
    int id,
    String terminology,
    String definition,
    String image,
    String audio,
    String pronounce,
    int level,
    int isRemember,
    String partOfSpeech,
    List<String> example,
    List<String> hint
){}
