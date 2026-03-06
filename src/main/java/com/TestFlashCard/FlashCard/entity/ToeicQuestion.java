package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "toeicQuestion")
@Getter
@Setter
@ToString(exclude = {"group", "exam", "options", "images", "questionReviews"})
@EqualsAndHashCode(exclude = {"group", "exam", "options", "images", "questionReviews"})
public class ToeicQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private Integer indexNumber;

    @Column(length = 50)
    private String part;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String detail;

    @Column(nullable = false, length = 1)
    private String result;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String clarify;

    @OneToMany(mappedBy = "toeicQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ToeicQuestionImage> images;

    @Column(length = 255)
    private String audio;   

    @Column
    private String conversation;

    @Column(name = "is_contribute")
    private Boolean isContribute = false;

    @Column(name = "bank_question_id")
    private Integer bankQuestionId;

    @CreationTimestamp
    @Column(name = "createAt", updatable = false)
    private LocalDateTime createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examID", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_question_id", nullable = true)
    private GroupQuestion group;

    @OneToMany(mappedBy = "toeicQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ToeicQuestionOption> options = new HashSet<>();

    @OneToMany(mappedBy = "toeicQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionReview> questionReviews;
}