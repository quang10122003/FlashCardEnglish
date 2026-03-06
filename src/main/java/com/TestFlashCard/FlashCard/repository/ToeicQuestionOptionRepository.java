package com.TestFlashCard.FlashCard.repository;

import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import com.TestFlashCard.FlashCard.entity.ToeicQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToeicQuestionOptionRepository extends JpaRepository<ToeicQuestionOption, Integer> {


}