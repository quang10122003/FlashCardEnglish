package com.TestFlashCard.FlashCard.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileUploadResponse {
    private String key;
    private String url;

    public FileUploadResponse(String key, String url) {
        this.key = key;
        this.url = url;
    }
}