package com.TestFlashCard.FlashCard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.TestFlashCard.FlashCard.entity.Exam;

@Repository
public interface IExam_Repository extends JpaRepository<Exam, Integer>, JpaSpecificationExecutor<Exam>{
    List<Exam>findAll();
    List<Exam> findByIsDeletedFalseOrderByCreatedAtDesc();
    List<Exam> findAllByOrderByCreatedAtDesc();
    List<Exam> findTop3ByOrderByAttempsDesc();
     // Thêm: Lấy exam hệ thống (không nằm trong custom_exam)
    @Query("SELECT e FROM Exam e WHERE e.id NOT IN (SELECT ce.customExam.id FROM CustomExam ce) AND e.isDeleted = false")
    List<Exam> findSystemExams();
    
    // Thêm: Lấy exam hệ thống với Specification (cho filter)
    @Query("SELECT e FROM Exam e WHERE e.id NOT IN (SELECT ce.customExam.id FROM CustomExam ce)")
    List<Exam> findAllSystemExams();
}