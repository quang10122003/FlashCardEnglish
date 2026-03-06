package com.TestFlashCard.FlashCard.response;

import java.time.LocalDate;

public record ListFlashCardsResponse(
    int id,
    String title,
    LocalDate reviewDate,
    int cycle,
    String learningStatus,
    int words
) {}