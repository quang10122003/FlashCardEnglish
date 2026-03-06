package com.TestFlashCard.FlashCard.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.VisitLog;

@Repository
public interface IVisitLog_Repository extends JpaRepository<VisitLog,Integer> {
    long countByVisitedAtBetween(LocalDateTime from, LocalDateTime to);
}
