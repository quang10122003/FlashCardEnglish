package com.TestFlashCard.FlashCard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.Comment;

@Repository
public interface IComment_Repository extends JpaRepository<Comment,Integer>{
    List<Comment> findByExamIdOrderByCreateAtDesc(Integer examId);
    int countByExamId(Integer examId);
}
