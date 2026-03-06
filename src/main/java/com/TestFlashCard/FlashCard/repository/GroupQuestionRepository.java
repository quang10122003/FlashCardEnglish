package com.TestFlashCard.FlashCard.repository;

import com.TestFlashCard.FlashCard.entity.Exam;
import com.TestFlashCard.FlashCard.entity.GroupQuestion;
import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupQuestionRepository extends JpaRepository<GroupQuestion, Integer> {
        @Query("""
                        select g.id
                        from GroupQuestion g
                        where g.exam.id = :examId
                        """)
        List<Integer> findGroupIdsByExamId(int examId);

        @Query("""
                        select distinct g from GroupQuestion g
                        left join fetch g.questions q
                        left join fetch q.options
                        left join fetch g.images
                        left join fetch g.audios
                        where g.id in :ids
                        """)
        List<GroupQuestion> findFullByIds(List<Integer> ids);

        @Query("""
                        select distinct g from GroupQuestion g
                        left join fetch g.images
                        left join fetch g.audios
                        where g.id in :ids
                        """)
        List<GroupQuestion> findGroupsWithMedia(List<Integer> ids);

        @Query("""
                        select g.bankGroupId
                        from GroupQuestion g
                        where g.exam.id = :examId
                        and g.bankGroupId in :bankGroupIds
                        """)
        List<Long> findUsedBankGroupIds(
                        int examId,
                        List<Long> bankGroupIds);
        List<GroupQuestion> findByExam(Exam exam);
        List<GroupQuestion> findByExamId(Integer examId);

}
