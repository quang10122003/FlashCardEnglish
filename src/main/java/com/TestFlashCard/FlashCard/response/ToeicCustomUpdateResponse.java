package com.TestFlashCard.FlashCard.response;

import lombok.Data;

@Data
public class ToeicCustomUpdateResponse {
    private Integer duration;
    private String title;
    private Integer attemps;
}
