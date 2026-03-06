package com.TestFlashCard.FlashCard.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "bank_group_question")
@Getter
@Setter
@ToString(exclude = {"questions", "images", "audios", "contributor"})
@EqualsAndHashCode(exclude = {"questions", "images", "audios", "contributor"})
public class BankGroupQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String part;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankGroupChildQuestion> questions = new ArrayList<>();



    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BankGroupImage> images = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BankGroupAudio> audios = new HashSet<>();


    private Boolean isPublic = true;

    @Column(name = "source_group_id", unique = true, nullable = false)
    private Integer sourceGroupId;


    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributor_id")
    private User contributor;
}

