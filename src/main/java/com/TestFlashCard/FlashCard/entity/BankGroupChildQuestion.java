package com.TestFlashCard.FlashCard.entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bank_group_child_question")
@Getter
@Setter
@ToString(exclude = {"group", "options"})
@EqualsAndHashCode(exclude = {"group", "options"})
public class BankGroupChildQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer indexNumber;

    @Column(columnDefinition = "TEXT")
    private String detail;

    private String result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_group_id")
    private BankGroupQuestion group;

    @OneToMany(mappedBy = "childQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankToeicOption> options = new ArrayList<>();

    private String clarify;
}

