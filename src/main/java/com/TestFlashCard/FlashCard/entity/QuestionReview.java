package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "questionReview")
@Data
public class QuestionReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examReviewID", nullable = false)
    private ExamReview examReview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toeicQuestionID", nullable = false)
    private ToeicQuestion toeicQuestion;

    @Column(length = 1, nullable = true)
    private String userAnswer;

    @CreationTimestamp
    @Column(name = "createAt", updatable = false)
    private LocalDateTime createAt;
}