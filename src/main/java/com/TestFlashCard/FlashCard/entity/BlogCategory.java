package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "blog_category")
public class BlogCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title", nullable = false)
    private String title;
}
