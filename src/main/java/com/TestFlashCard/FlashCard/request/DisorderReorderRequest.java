package com.TestFlashCard.FlashCard.request;

import lombok.Data;
import java.util.List;

@Data
public class DisorderReorderRequest {
    private List<ReorderItem> items;

    @Data
    public static class ReorderItem {
        private Integer id;
        private String type; // "single" hoặc "group"
        private Integer displayOrder;
    }
}