package com.TestFlashCard.FlashCard.response;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class CommentResponse {
    private Integer id;
    private String content;
    private String userName;
    private int userId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createAt;
    private String avatar;
    private List<CommentReplyResponse> replies;
}