package com.TestFlashCard.FlashCard.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bank_toeic_option")
@Getter
@Setter
public class BankToeicOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mark;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_question_id")
    private BankToeicQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_child_question_id")
    private BankGroupChildQuestion childQuestion;

}
