package com.TestFlashCard.FlashCard.request;

import lombok.Data;
import java.util.List;

@Data
public class DisorderGroupRequest {
    private String title;
    private String content;         // Passage/hội thoại
    private List<ImageRequest> images;
    private List<AudioRequest> audios;
    private List<DisorderQuestionRequest> questions; // Câu hỏi con (khi tạo mới)

    @Data
    public static class ImageRequest {
        private String url;
    }

    @Data
    public static class AudioRequest {
        private String url;
    }
}