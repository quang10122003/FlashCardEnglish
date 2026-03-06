package com.TestFlashCard.FlashCard.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisorderExamListResponse {
    private Integer id;
    private String title;
    private Integer duration;
    private Integer totalQuestions;
    private LocalDateTime createdAt;
}