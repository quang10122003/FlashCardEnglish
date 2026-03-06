package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.entity.ExamType;
import com.TestFlashCard.FlashCard.exception.ResourceExistedException;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IExamType_Repository;
import com.TestFlashCard.FlashCard.request.ExamTypeCreateRequest;
import com.TestFlashCard.FlashCard.request.ExamTypeUpdateRequest;
import com.TestFlashCard.FlashCard.response.ExamInformationResponse;

import jakarta.transaction.Transactional;

@Service
public class ExamTypeService {

    private final IExamType_Repository examType_Repository;
    private final ExamService examService;

    public ExamTypeService(IExamType_Repository examType_Repository, @Lazy ExamService examService) {
        this.examType_Repository = examType_Repository;
        this.examService = examService;
    }

    @Transactional
    public List<ExamType> getAllExamTypes() {
        return examType_Repository.findAll();
    }

    @Transactional
    public ExamType getDetailById(int id) throws IOException {
        ExamType examType = examType_Repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the ExamType with id: " + id));
        return examType;
    }

    @Transactional
    public ExamType getDetailByType(String type) throws IOException {
        ExamType examType = examType_Repository.findByType(type);
        if (examType == null)
            throw new ResourceNotFoundException("Cannot find ExamType with name: " + type);
        return examType;
    }

    @Transactional
    public void create(ExamTypeCreateRequest request) {
        if (examType_Repository.findByType(request.getType()) != null)
            throw new ResourceExistedException("ExamType name : " + request.getType() + " is existed!");
        ExamType examType = new ExamType();
        examType.setType(request.getType());
        examType_Repository.save(examType);
    }

    @Transactional
    public void update(ExamTypeUpdateRequest request, int id) throws IOException {
        ExamType examType = getDetailById(id);
        if (!examType.getType().equals(request.getType()))
            if (examType_Repository.findByType(request.getType()) != null)
                throw new ResourceExistedException("ExamType name : " + request.getType() + " is existed!");
        examType.setType(request.getType());
        examType_Repository.save(examType);
    }

    @Transactional
    public void softDelete(int id) throws IOException {
        ExamType examType = getDetailById(id);
        List<ExamInformationResponse> exams = examService.getByFilter(null, examType.getType(), null, null);
        System.out.println(exams);
        if (!exams.isEmpty())
            throw new BadRequestException("Cannot delete ExamType with id: " + id
                    + ". Please delete All data relatived before do the action.");
        examType_Repository.delete(examType);
    }
}
