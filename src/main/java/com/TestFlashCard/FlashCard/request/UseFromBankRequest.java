package com.TestFlashCard.FlashCard.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UseFromBankRequest {
    private List<Long> ids;
    private int examId;
}