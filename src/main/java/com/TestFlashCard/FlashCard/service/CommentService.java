package com.TestFlashCard.FlashCard.service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.Enum.Role;
import com.TestFlashCard.FlashCard.entity.Comment;
import com.TestFlashCard.FlashCard.entity.CommentReply;
import com.TestFlashCard.FlashCard.entity.Exam;
import com.TestFlashCard.FlashCard.repository.ICommentReply_Repository;
import com.TestFlashCard.FlashCard.repository.IComment_Repository;
import com.TestFlashCard.FlashCard.repository.IExam_Repository;
import com.TestFlashCard.FlashCard.request.CommentCreateRequest;
import com.TestFlashCard.FlashCard.request.CommentReplyCreateRequest;
import com.TestFlashCard.FlashCard.request.CommentUpdateRequest;
import com.TestFlashCard.FlashCard.response.CommentReplyResponse;
import com.TestFlashCard.FlashCard.response.CommentResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    @Autowired
    private MinIO_MediaService minIO_MediaService;
    @Autowired
    private final IComment_Repository comment_Repository;
    @Autowired
    private final ICommentReply_Repository commentReply_Repository;
    @Autowired
    private final IExam_Repository exam_Repository;

    @Transactional
    public void createComment(User user, CommentCreateRequest request) {
        Exam exam = exam_Repository.findById(request.getExamID()).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Exam with id: " + request.getExamID()));
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setExam(exam);
        comment.setUser(user);
        comment_Repository.save(comment);
    }

    @Transactional
    public void createReply(User user, CommentReplyCreateRequest request) {
        Comment comment = comment_Repository.findById(request.getCommentID()).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Comment with id: " + request.getCommentID()));

        CommentReply reply = new CommentReply();
        reply.setComment(comment);
        reply.setContent(request.getContent());
        reply.setUser(user);
        if (request.getParentReplyID() != null) {
            CommentReply parent = commentReply_Repository.findById(request.getParentReplyID())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent reply not found"));
            reply.setParentReply(parent);
        }
        commentReply_Repository.save(reply);
    }

    public List<CommentResponse> getCommentsByExamId(Integer examId) {
        List<Comment> comments = comment_Repository.findByExamIdOrderByCreateAtDesc(examId);

        return comments.stream().map(comment -> {

            CommentResponse cr = new CommentResponse();
            cr.setId(comment.getId());
            cr.setContent(comment.getContent());
            cr.setUserName(comment.getUser().getFullName());
            cr.setAvatar(minIO_MediaService.getPresignedURL(comment.getUser().getAvatar(), Duration.ofDays(1)));
            cr.setUserId(comment.getUser().getId());
            cr.setCreateAt(comment.getCreateAt());

            // reply cấp 1
            List<CommentReply> topReplies = comment.getReplies().stream()
                    .filter(r -> r.getParentReply() == null)
                    .collect(Collectors.toList());

            cr.setReplies(topReplies.stream().map(this::buildReplyTree).toList());

            return cr;
        }).toList();
    }

    private CommentReplyResponse buildReplyTree(CommentReply reply) {
        CommentReplyResponse rr = new CommentReplyResponse();
        rr.setId(reply.getId());
        rr.setContent(reply.getContent());
        rr.setUserName(reply.getUser().getFullName());
        rr.setCreateAt(reply.getCreateAt());
        rr.setAvatar(minIO_MediaService.getPresignedURL(reply.getUser().getAvatar(), Duration.ofDays(1)));
        rr.setUserId(reply.getUser().getId());

        List<CommentReplyResponse> children = reply.getChildren().stream()
                .map(this::buildReplyTree)
                .toList();

        rr.setReplies(children);
        return rr;
    }

    @Transactional
    public void deleteCommentById(Integer commentId, User requester) {
        Comment comment = comment_Repository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // Kiểm tra quyền nếu cần
        if (!(comment.getUser().getId()==requester.getId()) && requester.getRole() != Role.ADMIN) {
    throw new SecurityException("You are not allowed to delete this comment");
}

        comment_Repository.delete(comment); // sẽ xoá luôn cả replies nhờ orphanRemoval
    }

    @Transactional
    public void deleteReplyById(Integer replyId, User requester) {
        CommentReply reply = commentReply_Repository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException("Reply not found"));

        if (!(reply.getUser().getId()==requester.getId()) && requester.getRole() != Role.ADMIN) {
            throw new SecurityException("You are not allowed to delete this reply");
        }

        commentReply_Repository.delete(reply); // xoá luôn reply con nếu có, nhờ orphanRemoval
    }

    @Transactional
    public void updateComment(int commentId, CommentUpdateRequest request){
        Comment comment = comment_Repository.findById(commentId).orElseThrow(
            ()-> new ResourceNotFoundException("Cannot find the comment with id: " + commentId)
        );
        if(request.getContent()!=null)
        comment.setContent(request.getContent());

        comment_Repository.save(comment);
    }

    @Transactional
    public void updateCommentReply(int commentId, CommentUpdateRequest request){
        CommentReply reply = commentReply_Repository.findById(commentId).orElseThrow(
            ()-> new ResourceNotFoundException("Cannot find the reply with id: " + commentId)
        );
        if(request.getContent()!=null)
        reply.setContent(request.getContent());

        commentReply_Repository.save(reply);
    }


}
