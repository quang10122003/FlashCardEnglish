package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.response.FileUploadResponse;
import com.TestFlashCard.FlashCard.response.MediaUploadResultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class MediaUploadService {

    @Autowired
    private MinIO_MediaService minioService;

    public MediaUploadResultResponse uploadMedia(List<MultipartFile> images, List<MultipartFile> audios) throws IOException {

        List<FileUploadResponse> imageResults = new ArrayList<>();
        List<FileUploadResponse> audioResults = new ArrayList<>();

        // ---- Upload IMAGES ----
        if (images != null) {
            for (MultipartFile img : images) {
                String key = minioService.uploadFile(img);
                String url = minioService.getPresignedURL(key, Duration.ofHours(24));
                imageResults.add(new FileUploadResponse(key, url));
            }
        }

        // ---- Upload AUDIOS ----
        if (audios != null) {
            for (MultipartFile audio : audios) {
                String key = minioService.uploadFile(audio);
                String url = minioService.getPresignedURL(key, Duration.ofHours(24));
                audioResults.add(new FileUploadResponse(key, url));
            }
        }

        return new MediaUploadResultResponse(imageResults, audioResults);
    }
}
