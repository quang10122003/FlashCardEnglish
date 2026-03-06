package com.TestFlashCard.FlashCard.request;

import java.util.List;

import lombok.Data;

@Data
public class ToeicQuestionReorderRequest{
    private String part;
    private List<Item> items;

    @Data
    public static class Item {
        private Integer questionId;
        private Integer indexNumber;
        
    }
}