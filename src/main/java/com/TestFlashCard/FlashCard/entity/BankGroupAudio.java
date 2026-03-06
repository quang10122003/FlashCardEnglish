package com.TestFlashCard.FlashCard.entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Entity
@Table(name = "bank_group_audio")
@Getter
@Setter
@ToString(exclude = "group")
@EqualsAndHashCode(exclude = "group")
public class BankGroupAudio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String audioKey;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_group_id")
    private BankGroupQuestion group;
}

