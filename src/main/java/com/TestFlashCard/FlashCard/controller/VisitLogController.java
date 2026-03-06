package com.TestFlashCard.FlashCard.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.TestFlashCard.FlashCard.service.VisitLogService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/visit-log")
@RequiredArgsConstructor
public class VisitLogController {
    @Autowired
    private final VisitLogService visitLogService;

    @PostMapping("/record")
    public ResponseEntity<?> record() {
        visitLogService.create();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null));
    }

    @GetMapping("/count")
    public ResponseEntity<?> count(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) Integer months,
            @RequestParam(required = false) Integer years) {

        Map<String, Long> response = new LinkedHashMap<>();
        if (days != null)
            response.put("numOfVisited", visitLogService.countVisitsLastNDays(days));
        else if (months != null)
            response.put("numOfVisited", visitLogService.countVisitsLastNMonths(months));
        else if (years != null)
            response.put("numOfVisited", visitLogService.countVisitsLastNMonths(years));
        else
            response.put("numOfVisited", visitLogService.countAll());

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }
}
