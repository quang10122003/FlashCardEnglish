package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.entity.ExamCollection;
import com.TestFlashCard.FlashCard.exception.ResourceExistedException;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IExamCollection_Repository;
import com.TestFlashCard.FlashCard.request.ExamCollectionCreateRequest;
import com.TestFlashCard.FlashCard.request.ExamCollectionUpdateRequest;
import com.TestFlashCard.FlashCard.response.ExamInformationResponse;

import jakarta.transaction.Transactional;

@Service
public class ExamCollectionService {

    private final IExamCollection_Repository examCollection_Repository;
    private final ExamService examService;

    public ExamCollectionService(IExamCollection_Repository examCollection_Repository, @Lazy ExamService examService) {
        this.examCollection_Repository = examCollection_Repository;
        this.examService = examService;
    }

    @Transactional
    public List<ExamCollection> getAllExamCollection() {
        return examCollection_Repository.findByIsDeletedFalse();
    }

    @Transactional
    public ExamCollection getDetailById(int id) throws IOException {
        ExamCollection examCollection = examCollection_Repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the ExamCollection with id: " + id));
        return examCollection;
    }

    @Transactional
    public ExamCollection getDetailByCollection(String collection) throws IOException {
        ExamCollection examCollection = examCollection_Repository.findByCollection(collection);
        if (examCollection == null)
            throw new ResourceNotFoundException("Cannot find ExamCollection with name: " + collection);
        return examCollection;
    }

    @Transactional
    public void create(ExamCollectionCreateRequest request) {
        if (examCollection_Repository.findByCollection(request.getCollection()) != null) {
            throw new ResourceNotFoundException(
                    "This ExamCollection with name: " + request.getCollection() + " is exist!");
        }
        ExamCollection examCollection = new ExamCollection();
        examCollection.setCollection(request.getCollection());
        examCollection_Repository.save(examCollection);
    }

    @Transactional
    public void update(ExamCollectionUpdateRequest request, int id) throws IOException {
        ExamCollection examCollection = getDetailById(id);
        if (!examCollection.getCollection().equals(request.getCollection()))
            if (examCollection_Repository.findByCollection(request.getCollection()) != null)
                throw new ResourceExistedException("ExamCollection name : " + request.getCollection() + " is existed!");
        examCollection.setCollection(request.getCollection());
        examCollection_Repository.save(examCollection);
    }

    @Transactional
    public void Delete(int id) throws IOException {
        ExamCollection examCollection = getDetailById(id);

        List<ExamInformationResponse> exams = examService.getByFilter(null, null, examCollection.getCollection(), null);
        if (!exams.isEmpty())
            throw new BadRequestException("Cannot delete Exam Collection with id: " + id
                    + ". Please delete All data relatived before do the action");

        examCollection_Repository.delete(examCollection);
    }
}
