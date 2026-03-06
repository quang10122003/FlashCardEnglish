package com.TestFlashCard.FlashCard.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.entity.Exam;
import com.TestFlashCard.FlashCard.entity.TestAttempLog;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IAttempLog_Repository;
import com.TestFlashCard.FlashCard.repository.IExam_Repository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttempLogService {
    @Autowired
    private final IAttempLog_Repository attempLog_Repository;
    @Autowired
    private final IExam_Repository exam_Repository;

    @Transactional
    public void createAttemp(int examId, int userId) {
        TestAttempLog attempLog = new TestAttempLog();
        attempLog.setUserID(userId);
        attempLog.setExamID(examId);
        attempLog_Repository.save(attempLog);

        Exam exam = exam_Repository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find Exam with id: " + examId));
        exam.setAttemps(exam.getAttemps() + 1);
        exam_Repository.save(exam);
    }

    public long countTotal() {
        return attempLog_Repository.count();
    }

    public List<Map<String, Object>> countPerDayInRange(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        List<Object[]> raw = attempLog_Repository.countPerDay(startDateTime, endDateTime);

        // Map kết quả về LocalDate -> Long
        Map<LocalDate, Long> resultMap = new HashMap<>();
        for (Object[] row : raw) {
            LocalDate date = row[0] instanceof java.sql.Date
                    ? ((java.sql.Date) row[0]).toLocalDate()
                    : LocalDate.parse(row[0].toString());
            Long count = ((Number) row[1]).longValue();
            resultMap.put(date, count);
        }

        // Đảm bảo đủ các ngày, ngày không có thì count = 0
        List<Map<String, Object>> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", d.toString());
            entry.put("count", resultMap.getOrDefault(d, 0L));
            result.add(entry);
        }
        return result;
    }

    public List<Map<String, Object>> countPerDayLast30Days() {
        LocalDate today = LocalDate.now();
        LocalDate startDay = today.minusDays(29); // 30 ngày gần nhất
        LocalDateTime startDateTime = startDay.atStartOfDay();
        LocalDateTime endDateTime = today.atTime(LocalTime.MAX);

        List<Object[]> raw = attempLog_Repository.countPerDay(startDateTime, endDateTime);

        // Map kết quả về LocalDate -> Long
        Map<LocalDate, Long> resultMap = new HashMap<>();
        for (Object[] row : raw) {
            LocalDate date = row[0] instanceof java.sql.Date
                    ? ((java.sql.Date) row[0]).toLocalDate()
                    : LocalDate.parse(row[0].toString());
            Long count = ((Number) row[1]).longValue();
            resultMap.put(date, count);
        }

        // Đảm bảo đủ 30 ngày
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDate d = startDay.plusDays(i);
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", d.toString());
            entry.put("count", resultMap.getOrDefault(d, 0L));
            result.add(entry);
        }
        return result;
    }

    public List<Map<String, Object>> countPerMonthOfYear(int year) {
        int maxMonth = (year == Year.now().getValue()) ? LocalDate.now().getMonthValue() : 12;
        List<Object[]> raw = attempLog_Repository.countPerMonth(year);

        // Map kết quả từ query
        Map<Integer, Long> monthMap = new HashMap<>();
        for (Object[] row : raw) {
            int month = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            monthMap.put(month, count);
        }

        // Build kết quả đủ tháng
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 1; i <= maxMonth; i++) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("month", i);
            entry.put("count", monthMap.getOrDefault(i, 0L));
            result.add(entry);
        }
        return result;
    }
}
