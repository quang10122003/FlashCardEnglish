package com.TestFlashCard.FlashCard.response;

import java.time.LocalDateTime;

public record BlogResponse(
    int id,
    String title,
    String category,
    String shortDetail,
    String image,
    String detail,
    String author,
    LocalDateTime createAt
) {}
