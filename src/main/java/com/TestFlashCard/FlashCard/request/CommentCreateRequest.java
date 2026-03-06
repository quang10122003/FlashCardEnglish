package com.TestFlashCard.FlashCard.request;

import lombok.Data;

@Data
public class CommentCreateRequest {
    private Integer examID;
    private String content;
}
