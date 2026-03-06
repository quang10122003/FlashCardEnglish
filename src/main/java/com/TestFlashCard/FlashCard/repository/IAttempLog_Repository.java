package com.TestFlashCard.FlashCard.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.TestFlashCard.FlashCard.entity.TestAttempLog;

public interface IAttempLog_Repository extends JpaRepository<TestAttempLog, Integer> {
    int countByCreateAtBetween(LocalDateTime from, LocalDateTime to);

    @Query(value = "SELECT DATE(createAt) as day, COUNT(*) as count " +
            "FROM attemp_log " +
            "WHERE createAt >= :start AND createAt <= :end " +
            "GROUP BY day ORDER BY day", nativeQuery = true)
    List<Object[]> countPerDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT MONTH(createAt) as month, COUNT(*) as count " +
            "FROM attemp_log " +
            "WHERE YEAR(createAt) = :year " +
            "GROUP BY month ORDER BY month", nativeQuery = true)
    List<Object[]> countPerMonth(@Param("year") int year);
}
