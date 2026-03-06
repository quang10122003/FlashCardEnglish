package com.TestFlashCard.FlashCard.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.Exam;
import com.TestFlashCard.FlashCard.entity.ToeicQuestion;

@Repository
public interface IToeicQuestion_Repository extends JpaRepository<ToeicQuestion, Integer> {
    int countQuestionsByExamId(int examID);

    List<ToeicQuestion> findByExamId(int examID);

    List<ToeicQuestion> findAllByExam(Exam exam);

    List<ToeicQuestion> findAllByExamAndPartIn(Exam exam, Collection<String> parts);

    List<ToeicQuestion> findByExamIdAndPart(Integer examId, String part);

    int countByExam_Id(Integer id);

    /**
     * Lấy tất cả câu hỏi của exam theo ID
     */
    @Query("SELECT q FROM ToeicQuestion q WHERE q.exam.id = :examId ORDER BY q.indexNumber ASC")
    List<ToeicQuestion> findAllByExamId(@Param("examId") Integer examId);

    /**
     * Lấy tất cả câu hỏi của exam (bao gồm cả câu trong group)
     * Sorted by indexNumber
     */
    @Query("SELECT q FROM ToeicQuestion q WHERE q.exam = :exam ORDER BY q.indexNumber ASC")
    List<ToeicQuestion> findAllByExamOrderByIndexNumber(@Param("exam") Exam exam);
}
