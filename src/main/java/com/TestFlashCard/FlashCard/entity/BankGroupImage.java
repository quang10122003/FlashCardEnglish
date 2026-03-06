package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bank_group_image")
@Getter
@Setter
@ToString(exclude = "group")
@EqualsAndHashCode(exclude = "group")
public class BankGroupImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_group_id")
    private BankGroupQuestion group;
}

