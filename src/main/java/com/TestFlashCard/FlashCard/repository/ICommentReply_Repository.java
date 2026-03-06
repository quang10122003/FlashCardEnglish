package com.TestFlashCard.FlashCard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.CommentReply;

@Repository
public interface ICommentReply_Repository extends JpaRepository<CommentReply, Integer> {

    @Query("SELECT COUNT(r) FROM CommentReply r WHERE r.comment.exam.id = :examId")
    int countRepliesByExamId(@Param("examId") Integer examId);
}