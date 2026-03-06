package com.TestFlashCard.FlashCard.request;

import lombok.Data;

@Data
public class DisorderExamUpdateRequest {
    private String title;
    private Integer duration; // phút
}