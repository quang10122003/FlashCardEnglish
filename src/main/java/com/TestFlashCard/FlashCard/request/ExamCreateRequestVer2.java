package com.TestFlashCard.FlashCard.request;


import lombok.Data;

@Data
public class ExamCreateRequestVer2 {
    private Integer duration;

    private String title;

    private Integer year;

    private String type;

    private String collection;
}
