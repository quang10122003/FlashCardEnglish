package com.TestFlashCard.FlashCard.response;

import java.util.List;

import lombok.Data;

@Data
public class TotalRecordsResponse {
    private long feedbacks ;
    private long visits ;
    private long topics ;
    private long tests ;
    private List<ExamAttemp> top3Exam;
}
