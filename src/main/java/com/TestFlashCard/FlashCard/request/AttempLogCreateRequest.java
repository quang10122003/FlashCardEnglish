package com.TestFlashCard.FlashCard.request;

import lombok.Data;

@Data
public class AttempLogCreateRequest {
    private Integer userID;
    private Integer examID;
}
