package com.TestFlashCard.FlashCard.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "attemp_log")
public class TestAttempLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @CreationTimestamp
    @Column(name = "createAt", nullable = false)
    private LocalDateTime createAt;
    
    @Column(name = "userId")
    private Integer userID;

    @Column(name = "examId")
    private Integer examID;

}
