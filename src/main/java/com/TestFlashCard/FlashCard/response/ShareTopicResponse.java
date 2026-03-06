package com.TestFlashCard.FlashCard.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ShareTopicResponse {
    private boolean status;
    private String message;
}
