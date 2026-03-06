package com.TestFlashCard.FlashCard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.QuestionReview;

@Repository
public interface IToeicQuestionReview_Repository extends JpaRepository<QuestionReview,Integer>{
    
}
