package com.TestFlashCard.FlashCard.response;

public record ListFlashCardTopicResponse(
    int id,
    String title,
    String status,
    String learningStatus
) {}
