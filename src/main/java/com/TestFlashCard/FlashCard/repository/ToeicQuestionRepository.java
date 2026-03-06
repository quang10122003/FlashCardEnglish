package com.TestFlashCard.FlashCard.repository;

import com.TestFlashCard.FlashCard.entity.Exam;
import com.TestFlashCard.FlashCard.entity.ToeicQuestion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ToeicQuestionRepository extends JpaRepository<ToeicQuestion, Integer> {

        @Query("SELECT MAX(t.indexNumber) FROM ToeicQuestion t WHERE t.exam.id = :examId")
        Integer findMaxIndexByExam(Integer examId);

        @Query("SELECT MAX(t.indexNumber) FROM ToeicQuestion t WHERE t.exam.id = :examId AND t.part = :part")
        Integer findMaxIndexByExamAndPart(Integer examId, String part);

        @Query("""
                        select distinct q from ToeicQuestion q
                        left join fetch q.options
                        where q.group.id in :ids
                        """)
        List<ToeicQuestion> findQuestionsWithOptionsByGroupIds(List<Integer> ids);

        int countByExam_Id(Integer examId);

        int countByExam_IdAndPart(Integer examId, String part);

        @Query("""
                        select q.bankQuestionId from ToeicQuestion q
                        where q.exam.id = :examId and q.bankQuestionId in :bankIds
                        """)
        List<Integer> findUsedBankQuestionIds(
                        Integer examId,
                        List<Integer> bankIds);

        @Query("""
                            select q.id from ToeicQuestion q
                            where q.exam.id = :examId
                        """)
        List<Integer> findQuestionIdsByExamId(int examId);

        List<ToeicQuestion> findByExam(Exam exam);

        List<ToeicQuestion> findByExamId(Integer exam);
}