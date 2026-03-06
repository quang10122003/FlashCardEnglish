package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import com.TestFlashCard.FlashCard.Enum.FlashCardTopicStatus;
import com.TestFlashCard.FlashCard.Enum.LearningStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "flashCard_topic")
@Data
public class FlashCardTopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "visit_count", nullable = false)
    private Integer visitCount;

    @CreationTimestamp
    @Column(name = "createAt", updatable = false)
    private LocalDateTime createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('PRIVATE', 'PUBLIC') DEFAULT 'PUBLIC'")
    private FlashCardTopicStatus status;

    @Column(name = "status_gain")
    private Boolean statusGain;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_status", columnDefinition = "ENUM('MASTERED', 'IN_PROGRESS', 'NEW', 'REVIEW_NEEDED') DEFAULT 'NEW'")
    private LearningStatus leaningStatus;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<FlashCard>flashCards;
}