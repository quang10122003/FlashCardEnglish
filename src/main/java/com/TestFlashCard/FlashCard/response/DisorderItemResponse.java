package com.TestFlashCard.FlashCard.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Unified response cho cả Single Question và Group Question trong Disorder Exam
 * - type = "single": câu hỏi đơn
 * - type = "group": nhóm câu hỏi (có danh sách câu con)
 * 
 * ✅ UPDATED: Thêm audio fields vào ChildQuestionResponse
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisorderItemResponse {

    private Integer id;
    private String type; // "single" hoặc "group"
    private Integer displayOrder; // Thứ tự hiển thị

    // === Single Question Fields ===
    private String detail;
    private String result;
    private String clarify;
    private List<String> images; // Presigned URLs
    private List<String> imageKeys; // MinIO keys
    private String audio; // Presigned URL
    private String audioKey; // MinIO key
    private List<OptionResponse> options;
    private Boolean isContribute;
    private Integer bankQuestionId;

    // === Group Question Fields ===
    private String title;
    private String content; // Passage/hội thoại
    private String questionRange;
    private List<String> groupImages; // Presigned URLs
    private List<String> groupImageKeys;
    private List<String> groupAudios; // Presigned URLs
    private List<String> groupAudioKeys;
    private List<ChildQuestionResponse> questions; // Câu hỏi con
    private Boolean groupIsContribute;
    private Long bankGroupId;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptionResponse {
        private String mark;
        private String detail;
    }

    /**
     * ✅ UPDATED: Thêm audio và audioKey cho child question
     * (dự phòng cho tương lai nếu cần)
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChildQuestionResponse {
        private Integer id;
        private Integer indexNumber;
        private String detail;
        private String result;
        private String clarify;
        private List<String> images; // Presigned URLs
        private List<String> imageKeys; // MinIO keys
        private String audio; // ✅ NEW: Presigned URL (nullable)
        private String audioKey; // ✅ NEW: MinIO key (nullable)
        private List<OptionResponse> options;

        /**
         * Constructor không có audio (backward compatible)
         */
        public ChildQuestionResponse(
                Integer id,
                Integer indexNumber,
                String detail,
                String result,
                String clarify,
                List<String> images,
                List<String> imageKeys,
                List<OptionResponse> options) {
            this.id = id;
            this.indexNumber = indexNumber;
            this.detail = detail;
            this.result = result;
            this.clarify = clarify;
            this.images = images;
            this.imageKeys = imageKeys;
            this.audio = null;
            this.audioKey = null;
            this.options = options;
        }
    }
}