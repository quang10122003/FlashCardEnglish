package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "group_question_image")
@Data
public class GroupQuestionImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String url;// Quan trọng: Trường này lưu key, không phải url

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupQuestion group;
}

