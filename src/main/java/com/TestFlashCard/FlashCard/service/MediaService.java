package com.TestFlashCard.FlashCard.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.TestFlashCard.FlashCard.exception.InvalidImageException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MediaService {

    @Autowired
    private final MinIO_MediaService minioMediaService;

    public String getImageUrl(MultipartFile image) throws IOException {

        String imageFileUrl = null;

        if (image != null && !image.isEmpty()) {
            String contentType = image.getContentType();
            String fileExtension = getFileExtension(image.getOriginalFilename());

            // Xử lý trường hợp application/octet-stream dựa vào extension
            if (contentType != null && contentType.equals("application/octet-stream")
                    && isValidImageExtension(fileExtension)) {
                switch (fileExtension) {
                    case "jpg":
                        contentType = "image/jpg";
                        break;
                    case "jpeg":
                        contentType = "image/jpeg";
                        break;
                    case "png":
                        contentType = "image/png";
                        break;
                    case "gif":
                        contentType = "image/gif";
                        break;
                    case "webp":
                        contentType = "image/webp";
                        break;
                }
            }

            // Kiểm tra content-type sau khi đã xử lý
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new InvalidImageException("Invalid image's content type: " + contentType);
            }

            imageFileUrl = minioMediaService.uploadFile(image);
        }
        return imageFileUrl;
    }

    public String getImageUrl(File imageFile) throws IOException {
        if (imageFile == null || !imageFile.exists() || imageFile.length() == 0) {
            throw new InvalidImageException("Image file is invalid: " + imageFile);
        }

        String contentType = Files.probeContentType(imageFile.toPath());
        String extension = getFileExtension(imageFile.getName());

        if ("application/octet-stream".equals(contentType) && isValidImageExtension(extension)) {
            contentType = getImageContentTypeFromExtension(extension);
        }

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageException("Invalid image content type: " + contentType);
        }

        try (InputStream is = new FileInputStream(imageFile)) {
            final File imageFileCopy = imageFile;
            final String contentTypeCopy = contentType;
            MultipartFile multipartFile = new org.springframework.web.multipart.MultipartFile() {
                @Override
                public String getName() { return imageFileCopy.getName(); }
                @Override
                public String getOriginalFilename() { return imageFileCopy.getName(); }
                @Override
                public String getContentType() { return contentTypeCopy; }
                @Override
                public boolean isEmpty() { return imageFileCopy.length() == 0; }
                @Override
                public long getSize() { return imageFileCopy.length(); }
                @Override
                public byte[] getBytes() throws IOException { return java.nio.file.Files.readAllBytes(imageFileCopy.toPath()); }
                @Override
                public java.io.InputStream getInputStream() throws IOException { return new java.io.FileInputStream(imageFileCopy); }
                @Override
                public void transferTo(java.io.File dest) throws IOException, IllegalStateException { java.nio.file.Files.copy(imageFileCopy.toPath(), dest.toPath()); }
            };
            return minioMediaService.uploadFile(multipartFile);
        }
    }

    private String getImageContentTypeFromExtension(String ext) {
        return switch (ext) {
            case "jpg" -> "image/jpg";
            case "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> null;
        };
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    private boolean isValidImageExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        // Danh sách phần mở rộng hợp lệ
        List<String> validExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
        return validExtensions.contains(extension);
    }

    // Xử lý Audio
    public String getAudioUrl(MultipartFile audioFile) throws IOException {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("Audio file is empty.");
        }

        String contentType = audioFile.getContentType();
        String extension = getFileExtension(audioFile.getOriginalFilename());

        // Nếu content-type không đúng, đoán lại từ extension
        if ("application/octet-stream".equals(contentType)) {
            contentType = guessAudioContentType(extension);
        }

        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new IllegalArgumentException("Invalid audio content type: " + contentType);
        }

        return minioMediaService.uploadFile(audioFile);
    }

    public String getAudioUrl(File audioFile) throws IOException {
        if (audioFile == null || !audioFile.exists() || audioFile.length() == 0) {
            throw new IllegalArgumentException("Audio file is invalid.");
        }

        String contentType = Files.probeContentType(audioFile.toPath());
        String extension = getFileExtension(audioFile.getName());

        if ("application/octet-stream".equals(contentType)) {
            contentType = guessAudioContentType(extension);
        }

        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new IllegalArgumentException("Invalid audio content type: " + contentType);
        }

        try (InputStream is = new FileInputStream(audioFile)) {
            final File audioFileCopy = audioFile;
            final String contentTypeCopy = contentType;
            MultipartFile multipartFile = new org.springframework.web.multipart.MultipartFile() {
                @Override
                public String getName() { return audioFileCopy.getName(); }
                @Override
                public String getOriginalFilename() { return audioFileCopy.getName(); }
                @Override
                public String getContentType() { return contentTypeCopy; }
                @Override
                public boolean isEmpty() { return audioFileCopy.length() == 0; }
                @Override
                public long getSize() { return audioFileCopy.length(); }
                @Override
                public byte[] getBytes() throws IOException { return java.nio.file.Files.readAllBytes(audioFileCopy.toPath()); }
                @Override
                public java.io.InputStream getInputStream() throws IOException { return new java.io.FileInputStream(audioFileCopy); }
                @Override
                public void transferTo(java.io.File dest) throws IOException, IllegalStateException { java.nio.file.Files.copy(audioFileCopy.toPath(), dest.toPath()); }
            };
            return minioMediaService.uploadFile(multipartFile);
        }
    }

    private String guessAudioContentType(String extension) {
        return switch (extension) {
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            case "m4a" -> "audio/mp4";
            default -> null;
        };
    }
}
