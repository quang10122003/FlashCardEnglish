package com.TestFlashCard.FlashCard.controller;

import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.TestFlashCard.FlashCard.response.MediaUploadResultResponse;
import com.TestFlashCard.FlashCard.service.MediaUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media")
public class UploadController {


    private final MediaUploadService mediaUploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> upload(
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "audios", required = false) List<MultipartFile> audios
    ) {
        try {
            MediaUploadResultResponse result = mediaUploadService.uploadMedia(images, audios);
            return new ApiResponse<>(HttpStatus.ACCEPTED.value(), "Upload ảnh hoặc media thành công",result);

        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Upload ảnh hoặc media thất bại vì: "+e.getMessage());
        }
    }
}
