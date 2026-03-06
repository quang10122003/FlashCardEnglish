package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bank_image")
@Data
public class BankImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url; // cũng là key MinIO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_question_id")
    private BankToeicQuestion question;
}
