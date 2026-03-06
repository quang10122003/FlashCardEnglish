package com.TestFlashCard.FlashCard.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.entity.VisitLog;
import com.TestFlashCard.FlashCard.repository.IVisitLog_Repository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisitLogService {
    private final IVisitLog_Repository visitLog_Repository;

    public void create(){
        VisitLog log=new VisitLog();
        visitLog_Repository.save(log);
    }
    public long countAll(){
        return visitLog_Repository.count();
    }
    public long countVisitsLastNDays(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(days);
        return visitLog_Repository.countByVisitedAtBetween(from, now);
    }

    public long countVisitsLastNMonths(int months) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusMonths(months);
        return visitLog_Repository.countByVisitedAtBetween(from, now);
    }

    public long countVisitsLastNYears(int years) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusYears(years);
        return visitLog_Repository.countByVisitedAtBetween(from, now);
    }
}
