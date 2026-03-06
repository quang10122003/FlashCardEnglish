package com.TestFlashCard.FlashCard.repository;


import com.TestFlashCard.FlashCard.entity.BankGroupChildQuestion;
import com.TestFlashCard.FlashCard.entity.BankGroupQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankGroupQuestionRepository
        extends JpaRepository<BankGroupQuestion, Long> {
    @Query("""
    SELECT b.sourceGroupId FROM BankGroupQuestion b
    WHERE b.sourceGroupId IN :ids
    """)
    List<Integer> findExistingSourceIds( List<Integer> ids);

    @Query("""
            select distinct b from BankGroupQuestion b
            left join fetch b.images
            left join fetch b.audios
            where b.sourceGroupId in :ids
        """)
    List<BankGroupQuestion> findBySourceGroupIds(List<Integer> ids);

    // group + media
    @Query("""
        select distinct g from BankGroupQuestion g
        left join fetch g.images
        left join fetch g.audios
        where g.isPublic = true
          and (:part is null or g.part = :part)
    """)
    List<BankGroupQuestion> findGroupsForUse( String part);

    @Query("""
    select distinct g from BankGroupQuestion g
    left join fetch g.images
    left join fetch g.audios
    where g.id in :ids
""")
    List<BankGroupQuestion> findGroupsWithMedia(List<Long> ids);
    @Query("""
    select distinct c from BankGroupChildQuestion c
    left join fetch c.options
    where c.group.id in :ids
""")
    List<BankGroupChildQuestion> findChildrenWithOptions(List<Long> ids);



        @Query("""
    select g from BankGroupQuestion g
    left join fetch g.images
    left join fetch g.audios
    where g.id = :id
    """)
    Optional<BankGroupQuestion> findGroupWithMedia(Long id);


}
