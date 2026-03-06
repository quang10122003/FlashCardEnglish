package com.TestFlashCard.FlashCard.entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "group_question")
@Getter
@Setter
@ToString(exclude = {"questions", "images", "audios", "exam"})
@EqualsAndHashCode(exclude = {"questions", "images", "audios", "exam"})
public class GroupQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 20)
    private String part;  // 3,4,7

    @Column(length = 255)
    private String title; // mô tả ngắn

    @Column(columnDefinition = "TEXT")
    private String content; // hội thoại/bài nói/bài đọc

    @Column(name = "is_contribute")
    private Boolean isContribute = false;

    @Column(name = "bank_group_id")
    private Long bankGroupId;

    @Column(length = 50)
    private String questionRange; // "32-35"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ToeicQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupQuestionImage> images = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupQuestionAudio> audios = new HashSet<>();
}

