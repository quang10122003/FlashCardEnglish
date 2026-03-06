package com.TestFlashCard.FlashCard.repository;

import com.TestFlashCard.FlashCard.entity.BankGroupChildQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankGroupChildQuestionRepository extends JpaRepository<BankGroupChildQuestion, Integer> {
    @Query("""
select distinct c from BankGroupChildQuestion c
left join fetch c.options
where c.group.id in :ids
""")
    List<BankGroupChildQuestion> findChildrenWithOptionsByGroupIds(List<Long> ids);


    @Query("""
select distinct c from BankGroupChildQuestion c
left join fetch c.options
where c.group.id = :id
""")
    List<BankGroupChildQuestion> findChildrenWithOptionsByGroupId(Long id);
}
