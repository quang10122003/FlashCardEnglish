package com.TestFlashCard.FlashCard.response;

import java.time.LocalDateTime;

public record EvaluateResponse(
    int id,
    String content,
    int star,
    String image,
    LocalDateTime createAt,
    String fullName,
    String email,
    String avatar,
    String adminReply,
    LocalDateTime replyAt
) {}
