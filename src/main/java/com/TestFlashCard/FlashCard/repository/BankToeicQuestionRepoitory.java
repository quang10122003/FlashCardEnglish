package com.TestFlashCard.FlashCard.repository;

import com.TestFlashCard.FlashCard.entity.BankToeicQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankToeicQuestionRepoitory
        extends JpaRepository<BankToeicQuestion, Long> {

    @Query("""
        select b.sourceToeicId
        from BankToeicQuestion b
        where b.sourceToeicId in :ids
    """)
    List<Integer> findContributedToeicIds(@Param("ids") List<Integer> ids);

    @Query("""
   select b from BankToeicQuestion b
   where b.sourceToeicId in :ids
""")
    List<BankToeicQuestion> findBySourceToeicIds(@Param("ids") List<Integer> ids);


    @Query("""
        select distinct q from BankToeicQuestion q
        left join fetch q.options
        left join fetch q.images
        where q.id in :ids
    """)
    List<BankToeicQuestion> findFullByIds(List<Integer> ids);
    @Query("""
    select distinct q from BankToeicQuestion q
    left join fetch q.images
    where q.id in :ids
    """)
    List<BankToeicQuestion> findWithImages(List<Integer> ids);

    @Query("""
select distinct q from BankToeicQuestion q
left join fetch q.images
where q.id in :ids
""")
    List<BankToeicQuestion> findWithImagesByIds(List<Integer> ids);


    @Query("""
select q from BankToeicQuestion q
left join fetch q.images
where q.id = :id
""")
    Optional<BankToeicQuestion> findWithImagesById(Integer id);

}
