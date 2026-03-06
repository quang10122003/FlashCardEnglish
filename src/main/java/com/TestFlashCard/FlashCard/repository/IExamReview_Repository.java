package com.TestFlashCard.FlashCard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.Exam;
import com.TestFlashCard.FlashCard.entity.ExamReview;
import com.TestFlashCard.FlashCard.entity.User;

@Repository
public interface IExamReview_Repository extends JpaRepository<ExamReview,Integer> {
    List<ExamReview> findByUserAndExam(User user, Exam exam);
}
