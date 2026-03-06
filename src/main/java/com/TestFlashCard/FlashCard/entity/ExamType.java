package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "exam_type")
public class ExamType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "type", nullable = false)
    private String type;
    
    @Column(name = "isDeleted", nullable = false)
    private boolean isDeleted = false;
}
