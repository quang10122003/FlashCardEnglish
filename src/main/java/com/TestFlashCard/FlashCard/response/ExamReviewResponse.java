package com.TestFlashCard.FlashCard.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ExamReviewResponse {
    private Integer reviewId;
    private Integer examID;
    private Integer userID;
    private String userName;
    private String examTitle;
    private String examCollection;
    private String selectedPart;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer incorrectAnswers;
    private Integer nullAnswers;
    private Integer duration;
    private LocalDateTime createdAt;
    private String section;
    private List<QuestionReviewResponse> questionReviews;
}
