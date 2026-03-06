package com.TestFlashCard.FlashCard.response;

import lombok.Data;
import java.util.List;

@Data
public class QuestionReviewResponse {
    
    // === Basic Question Info ===
    private Integer questionId;
    private Integer indexNumber;
    private String part;
    private String detail;
    private List<String> images;
    private String audio;
    private String conversation;
    private String clarify;
    
    // === Answer Info ===
    private String userAnswer;
    private String correctAnswer;
    private boolean isCorrect;
    
    // === Options ===
    private List<OptionResponse> options;
    
    // === Group Information (NEW) ===
    // Các field này chỉ có giá trị khi câu hỏi thuộc một GroupQuestion
    private Integer groupId;
    private String groupContent;        // Passage/hội thoại chung của group
    private String groupQuestionRange;  // "32-34", "41-43"...
    private List<String> groupImages;   // Ảnh chung của group
    private List<String> groupAudios;   // Audio chung của group

    @Data
    public static class OptionResponse {
        private String mark;
        private String detail;
    }
}