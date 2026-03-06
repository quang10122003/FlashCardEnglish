package com.TestFlashCard.FlashCard.repository;

import com.TestFlashCard.FlashCard.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, Integer> {
    
}
