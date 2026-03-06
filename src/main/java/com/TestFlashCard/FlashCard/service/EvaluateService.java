package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.math3.analysis.function.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.JpaSpec.EvaluateSpecification;
import com.TestFlashCard.FlashCard.entity.Evaluate;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IEvaluate_Repository;
import com.TestFlashCard.FlashCard.request.EvaluateCreateRequest;
import com.TestFlashCard.FlashCard.request.EvaluateUpdateByUserRequest;
import com.TestFlashCard.FlashCard.response.EvaluateResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvaluateService {
    @Autowired
    private final IEvaluate_Repository evaluate_Repository;

    @Autowired
    private MinIO_MediaService minIO_MediaService;

    @Autowired
    private MinIO_MediaService mediaService;

    public void createEvaluate(EvaluateCreateRequest request, MultipartFile imagFile, User user) throws IOException {

        Evaluate evaluate = new Evaluate();
        evaluate.setContent(request.getContent());
        evaluate.setStar(request.getStar());
        evaluate.setUser(user);

        if (imagFile != null) {
            String image = minIO_MediaService.uploadFile(imagFile);
            evaluate.setImage(image);
        } else {
            evaluate.setImage(null);
        }

        evaluate.setAdminReply(
                "Đội ngũ Admin chân thành cảm ơn bài đánh giá của bạn. Chúng tôi luôn cảm kích với những ý kiến từ bạn để xây dựng trang web ngày càng hoàn thiện. Trân trọng. ");
        evaluate.setReplyAt(LocalDateTime.now());
        evaluate_Repository.save(evaluate);
    }

    public void deleteEvaluate(int evaluateID) {
        Evaluate evaluate = evaluate_Repository.findById(evaluateID).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the evaluate with id: " + evaluateID));

        if (evaluate.getImage() != null)
            minIO_MediaService.deleteFile(evaluate.getImage());

        evaluate_Repository.delete(evaluate);
    }

    public List<EvaluateResponse> getAllEvaluates() {
        return evaluate_Repository.findAll().stream().map(this::convertToEvaluateResponse).toList();
    }

    public List<EvaluateResponse> getEvaluatesByStar(int star) throws IOException {
        Specification<Evaluate> evaluateSpecification = Specification.where(EvaluateSpecification.hasStar(star));
        return evaluate_Repository.findAll(evaluateSpecification).stream().map(this::convertToEvaluateResponse)
                .toList();
    }

    public void update(String adminReply, int id) throws IOException {
        Evaluate evaluate = evaluate_Repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the evaluate with id: " + id));
        
        if (adminReply != null){
            evaluate.setAdminReply(adminReply);
            evaluate.setReplyAt(LocalDateTime.now());
        }
        evaluate_Repository.save(evaluate);
    }

    private EvaluateResponse convertToEvaluateResponse(Evaluate evaluate) {
        String image = null;
        String avatar = null;
        if(evaluate.getImage()!=null && !evaluate.getImage().isEmpty())
            image = minIO_MediaService.getPresignedURL(evaluate.getImage(), Duration.ofMinutes(1));
        if(evaluate.getUser().getAvatar()!=null){
            avatar = minIO_MediaService.getPresignedURL(evaluate.getUser().getAvatar(), Duration.ofDays(1));
        }
        return new EvaluateResponse(
                evaluate.getId(),
                evaluate.getContent(),
                evaluate.getStar(),
                image,
                evaluate.getCreateAt(),
                evaluate.getUser().getFullName(),
                evaluate.getUser().getEmail(),
                avatar,
                evaluate.getAdminReply(),
                evaluate.getReplyAt());
    }

    public long countAll() {
        return evaluate_Repository.count();
    }

    public EvaluateResponse getByUser(User user) {
        Evaluate evaluate = evaluate_Repository.findByUser(user);
        if(evaluate==null) return null;
        return convertToEvaluateResponse(evaluate);
    }

    @Transactional
    public void updateByUser(User user, EvaluateUpdateByUserRequest request, MultipartFile image) throws IOException {
        Evaluate evaluate = evaluate_Repository.findByUser(user);
        if (evaluate == null)
            throw new ResourceNotFoundException("Cannot find the evaluate of user : " + user.getAccountName());

        if (request.getContent() != null)
            evaluate.setContent(request.getContent());
        if (request.getStar() != null)
            evaluate.setStar(request.getStar());

        if (image != null) {
            if (evaluate.getImage() != null)
                mediaService.deleteFile(evaluate.getImage());
            evaluate.setImage(mediaService.uploadFile(image));
        }

        evaluate_Repository.save(evaluate);
    }
}
