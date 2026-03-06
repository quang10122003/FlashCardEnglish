package com.TestFlashCard.FlashCard.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "visit_log")
public class VisitLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @CreationTimestamp
    @Column(name = "visitedAt", updatable = false)
    private LocalDateTime visitedAt;
}
