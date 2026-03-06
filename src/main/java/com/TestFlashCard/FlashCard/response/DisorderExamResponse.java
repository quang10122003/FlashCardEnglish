package com.TestFlashCard.FlashCard.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisorderExamResponse {
    private Integer id;
    private String title;
    private Integer duration;
    private Integer totalQuestions;
    private LocalDateTime createdAt;
    
    // Tất cả items (single + group) đã sort theo displayOrder
    private List<DisorderItemResponse> items;
}