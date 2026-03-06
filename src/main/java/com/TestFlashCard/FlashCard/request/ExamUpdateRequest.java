package com.TestFlashCard.FlashCard.request;

import lombok.Data;

@Data
public class ExamUpdateRequest {
    private Integer duration;
    private String title;
    private Integer year;
    private String type;
    private String collection;
    private Integer attemps;
}
