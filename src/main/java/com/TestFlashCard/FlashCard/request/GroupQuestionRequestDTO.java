package com.TestFlashCard.FlashCard.request;

import lombok.Data;

import java.util.List;

@Data
public class GroupQuestionRequestDTO {
    private String part;
    private String title;
    private String content;
    private String questionRange;
    private Integer examId;

    private List<GroupQuestionImageRequest> images;
    private List<GroupQuestionAudioRequest> audios;

    private List<ToeicQuestionRequestDTO> questions;
}
