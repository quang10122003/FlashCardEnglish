package com.TestFlashCard.FlashCard.response;

public record FlashCardTopicPublicResponse(
    int id,
    String userName,
    String title,
    String status,
    int visitCount
) {}
