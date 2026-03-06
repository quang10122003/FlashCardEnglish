package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.TestFlashCard.FlashCard.Enum.LearningStatus;

@Data
@Entity
@Table(name = "flashCard")
public class FlashCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "reviewDate")
    private LocalDate reviewDate;

    @Column (name = "cycle", nullable = false)
    private int cycle;

    @Enumerated(EnumType.STRING)
    @Column (name = "learningStatus", nullable = false, columnDefinition ="ENUM('MASTERED', 'IN_PROGRESS', 'NEW', 'REVIEW_NEEDED') DEFAULT 'NEW'")
    private LearningStatus learningStatus;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topicID", nullable = false)
    private FlashCardTopic topic;

    @OneToMany(mappedBy = "flashCard",cascade = CascadeType.ALL)
    private List<Card> cards;
}

