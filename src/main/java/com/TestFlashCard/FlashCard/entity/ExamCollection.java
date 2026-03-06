package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "exam_collection")
public class ExamCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "collection", nullable = false)
    private String collection;

    @Column(name = "isDeleted", nullable = false)
    private boolean isDeleted = false;
}
