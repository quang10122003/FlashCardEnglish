package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "examReview")
@Data
public class ExamReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examID", nullable = false)
    private Exam exam;

    @CreationTimestamp
    @Column(name = "createAt", updatable = false)
    private LocalDateTime createAt;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false)
    private String selectedPart;

    @Column(nullable = false)
    private Integer result;

    @Column
    private Integer incorrect;

    @OneToMany(mappedBy = "examReview", cascade = CascadeType.ALL)
    private List<QuestionReview> questionReviews;
}