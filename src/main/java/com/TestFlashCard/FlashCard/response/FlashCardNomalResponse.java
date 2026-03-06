package com.TestFlashCard.FlashCard.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FlashCardNomalResponse {
    List<CardsResponse> listCardResponse;
    List<CardChoiceRespoinse> listCardChoice;
}
