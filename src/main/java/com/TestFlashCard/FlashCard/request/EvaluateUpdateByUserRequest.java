package com.TestFlashCard.FlashCard.request;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EvaluateUpdateByUserRequest {
    private String content;
    private Integer star;
}
