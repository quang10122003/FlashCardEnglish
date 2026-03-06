package com.TestFlashCard.FlashCard.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomExamResponse {
    private Integer id;
    private String title;
    private Integer duration;
    
    // ✅ OPTION 3: Dùng Boolean wrapper - Jackson sẽ serialize đúng tên "isRandom"
    private Boolean isRandom;
    
    private Integer year;
    private LocalDateTime createdAt;
    private Integer totalQuestions;
}