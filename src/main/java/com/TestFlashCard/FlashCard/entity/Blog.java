package com.TestFlashCard.FlashCard.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "blog")
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private BlogCategory category;

    @Column(name = "short_detail", nullable = false, columnDefinition = "TEXT")
    private String shortDetail;

    @Column(name = "image")
    private String image;

    @Column(name = "detail", nullable = false, columnDefinition = "LONGTEXT")
    private String detail;

    @Column (name = "createAt")
    private LocalDateTime createAt;

    @Column (name = "author")
    private String author;
}
