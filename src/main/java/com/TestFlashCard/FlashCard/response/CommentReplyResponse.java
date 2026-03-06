package com.TestFlashCard.FlashCard.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class CommentReplyResponse {
    private Integer id;
    private String content;
    private String userName;
    private int userId;
    private String avatar;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createAt;
    private List<CommentReplyResponse> replies; // reply lồng nhau
}
