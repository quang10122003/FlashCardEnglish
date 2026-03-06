package com.TestFlashCard.FlashCard.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TestFlashCard.FlashCard.request.AttempLogCreateRequest;
import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.TestFlashCard.FlashCard.response.TotalRecordsResponse;
import com.TestFlashCard.FlashCard.service.AttempLogService;
import com.TestFlashCard.FlashCard.service.EvaluateService;
import com.TestFlashCard.FlashCard.service.ExamService;
import com.TestFlashCard.FlashCard.service.FlashCardService;
import com.TestFlashCard.FlashCard.service.VisitLogService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RequestMapping("api/dashboard")
@RestController
@RequiredArgsConstructor

public class DashboardController {
    @Autowired
    private final AttempLogService attempLogService;
    @Autowired
    private final EvaluateService evaluateService;
    @Autowired
    private final VisitLogService visitLogService;
    @Autowired
    private final FlashCardService flashCardService;
    @Autowired
    private final ExamService examService;

    @GetMapping("/totalRecords")
    public ResponseEntity<?> getTotalRecord() {
        TotalRecordsResponse response = new TotalRecordsResponse();
        response.setFeedbacks(evaluateService.countAll());
        response.setVisits(visitLogService.countAll());
        response.setTopics(flashCardService.countAllTopic());
        response.setTests(examService.countAll());
        response.setTop3Exam(examService.getTop3ExamByAttemps());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @PostMapping("/attempLog/create")
    public ResponseEntity<?> createAttempLog(@RequestBody AttempLogCreateRequest request) {
        attempLogService.createAttemp(request.getExamID(), request.getUserID());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
    }
    
    
    @GetMapping("/attempLog/countPerDay")
    public ResponseEntity<?> countAttempPerDayInRange(@RequestParam String start, @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(attempLogService.countPerDayInRange(startDate, endDate)));
    }
}
