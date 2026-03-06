package com.TestFlashCard.FlashCard.repository;

import com.TestFlashCard.FlashCard.entity.BankToeicOption;
import com.TestFlashCard.FlashCard.entity.BankToeicQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankToeicOptionRepoitory
        extends JpaRepository<BankToeicOption, Long> {
        @Query("""
    select distinct o from BankToeicOption o
    where o.question.id in :ids
    """)
        List<BankToeicOption> findOptionsByQuestionIds(List<Integer> ids);

        @Query("""
select o from BankToeicOption o
where o.question.id in :ids
""")
        List<BankToeicOption> findByQuestionIds(List<Integer> ids);


        @Query("""
select o from BankToeicOption o
where o.question.id = :id
""")
        List<BankToeicOption> findByQuestionId(Integer id);
}
