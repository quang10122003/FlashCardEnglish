package com.TestFlashCard.FlashCard.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class MediaUploadResultResponse {
    private List<FileUploadResponse> images;
    private List<FileUploadResponse> audios;

    public MediaUploadResultResponse(List<FileUploadResponse> images, List<FileUploadResponse> audios) {
        this.images = images;
        this.audios = audios;
    }
}
