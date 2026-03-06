package com.TestFlashCard.FlashCard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.ExamType;

@Repository
public interface IExamType_Repository extends JpaRepository<ExamType,Integer> {
    List<ExamType>findByIsDeletedFalse();
    ExamType findByTypeAndIsDeletedFalse(String type);
    ExamType findByType(String type);
}
