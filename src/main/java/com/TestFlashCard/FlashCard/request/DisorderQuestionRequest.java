package com.TestFlashCard.FlashCard.request;

import lombok.Data;
import java.util.List;

@Data
public class DisorderQuestionRequest {
    private String detail;
    private String result; // "A", "B", "C", "D"
    private String clarify;
    private String audio; // MinIO key (nullable)
    private List<OptionRequest> options;
    private List<ImageRequest> images;

    @Data
    public static class OptionRequest {
        private String mark;
        private String detail;
    }

    @Data
    public static class ImageRequest {
        private String url; // MinIO key
    }
}