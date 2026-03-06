package com.TestFlashCard.FlashCard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.Evaluate;
import com.TestFlashCard.FlashCard.entity.User;

@Repository
public interface IEvaluate_Repository extends JpaRepository<Evaluate,Integer>, JpaSpecificationExecutor<Evaluate>{
    List<Evaluate>findAll();
    Evaluate findByUser(User user);
}
