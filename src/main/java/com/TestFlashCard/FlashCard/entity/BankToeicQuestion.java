package com.TestFlashCard.FlashCard.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bank_toeic_question")
@Getter
@Setter
public class BankToeicQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String part;

    @Column(columnDefinition = "TEXT")
    private String detail;

    private String result;

    private String audio;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankToeicOption> options;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankImage> images;

    private String clarify;

    private Boolean isPublic = true;

    @Column(name = "source_toeic_id", nullable = false, unique = true)
    private Integer sourceToeicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributor_id")
    private User contributor;


    @CreationTimestamp
    private LocalDateTime createdAt;
}

