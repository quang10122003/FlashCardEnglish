package com.TestFlashCard.FlashCard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.ExamCollection;

@Repository
public interface IExamCollection_Repository extends JpaRepository<ExamCollection,Integer>{
    List<ExamCollection>findByIsDeletedFalse();
    ExamCollection findByCollectionAndIsDeletedFalse(String collection);
    ExamCollection findByCollection(String collection);
}