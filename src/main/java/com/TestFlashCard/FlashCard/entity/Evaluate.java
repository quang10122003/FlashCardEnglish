package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluate")
@Data
public class Evaluate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer star;

    @Column(name = "image")
    private String image;

    @Column(name = "admin_reply", nullable = true, columnDefinition = "TEXT")
    private String adminReply;

    @Column (name = "replyAt", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP" )
    private LocalDateTime replyAt;

    @CreationTimestamp
    @Column(name = "createAt", updatable = false)
    private LocalDateTime createAt;
}