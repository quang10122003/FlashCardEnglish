package com.TestFlashCard.FlashCard.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.TestFlashCard.FlashCard.entity.ToeicQuestionImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.config.MinIOProperties;
import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import com.TestFlashCard.FlashCard.exception.InvalidImageException;

import jakarta.transaction.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class MinIO_MediaService {

    @Autowired private S3Client s3Client;
    // Nếu bạn đặt @Bean tên "publicPresigner" trong MinIOConfig, nên dùng @Qualifier cho chắc
    // @Autowired @Qualifier("publicPresigner")
    @Autowired
    @Qualifier("publicPresigner")
    private S3Presigner publicPresigner;

    @Autowired private MinIOProperties minIOProperties;

    public static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg","image/jpg","image/png","image/gif","image/webp","application/octet-stream"
    );

    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType = file.getContentType();
        String fileExtension = getFileExtension(file.getOriginalFilename());
        contentType = normalizeContentType(contentType, fileExtension);

        // Cho phép image *hoặc* audio
        if (contentType == null ||
            !(contentType.startsWith("image/") || contentType.startsWith("audio/"))) {
            throw new InvalidImageException("Invalid content type: " + contentType);
        }

        String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());

        System.out.println("Uploading to bucket: " + minIOProperties.getBucket());
        System.out.println("Key: " + uniqueFileName);
        System.out.println("Content-Type: " + contentType);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(minIOProperties.getBucket())
                .key(uniqueFileName)
                .contentType(contentType)
                .build();

        try {
            // Tránh giữ file lớn trong heap: dùng stream
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (S3Exception e) {
            System.err.println("❌ S3Exception:");
            System.err.println("  StatusCode: " + e.statusCode());
            if (e.awsErrorDetails() != null) {
                System.err.println("  ErrorCode: " + e.awsErrorDetails().errorCode());
                System.err.println("  Message: " + e.awsErrorDetails().errorMessage());
            }
            throw e;
        }

        // Lưu **key** vào DB; khi trả về FE hãy presign bằng getPresignedURL(...)
        return uniqueFileName;
    }

    public String uploadFile(File imageFile) throws IOException {
        if (imageFile == null || !imageFile.exists() || imageFile.length() == 0) {
            throw new InvalidImageException("Image file is empty or not found");
        }

        final String originalFilename = imageFile.getName();
        final String ext = getFileExtension(originalFilename);

        String contentType = Files.probeContentType(imageFile.toPath());
        contentType = normalizeContentType(contentType, ext);

        if (contentType == null ||
            !(contentType.startsWith("image/") || contentType.startsWith("audio/"))) {
            throw new InvalidImageException("Invalid content type: " + contentType);
        }

        String uniqueFileName = generateUniqueFileName(originalFilename);

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(minIOProperties.getBucket())
                .key(uniqueFileName)
                .contentType(contentType)
                .build();

        try {
            s3Client.putObject(req, RequestBody.fromFile(imageFile));
        } catch (S3Exception e) {
            System.err.println("❌ S3Exception:");
            System.err.println("  StatusCode: " + e.statusCode());
            if (e.awsErrorDetails() != null) {
                System.err.println("  ErrorCode: " + e.awsErrorDetails().errorCode());
                System.err.println("  Message: " + e.awsErrorDetails().errorMessage());
            }
            throw e;
        }

        return uniqueFileName;
    }

    private String normalizeContentType(String contentType, String extension) {
        if (contentType != null && !contentType.equals("application/octet-stream")) {
            return contentType;
        }
        if (extension == null) return null;

        switch (extension) {
            case "jpg":  return "image/jpeg"; // chuẩn hóa về image/jpeg
            case "jpeg": return "image/jpeg";
            case "png":  return "image/png";
            case "gif":  return "image/gif";
            case "webp": return "image/webp";
            case "mp3":  return "audio/mpeg";
            case "wav":  return "audio/wav";
            case "ogg":  return "audio/ogg";
            case "m4a":  return "audio/mp4";
            default:     return null;
        }
    }

    // Presign URL dùng publicPresigner (đã ký với public-endpoint)
    @Cacheable(value = "presignedUrls", key = "#key")   // ✅ sửa #fileName -> #key
    public String getPresignedURL(String key, Duration expiry) {
        expiry = Duration.ofDays(1);
        var get = GetObjectRequest.builder()
                .bucket(minIOProperties.getBucket())
                .key(key)
                .responseCacheControl("public, max-age=31536000, immutable")
                .build();
        var req = GetObjectPresignRequest.builder()
                .signatureDuration(expiry)
                .getObjectRequest(get)
                .build();
        return publicPresigner.presignGetObject(req).url().toString();
    }

    private String generateUniqueFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "-" + originalFileName;
    }

    // Hỗ trợ xóa cả khi người dùng lỡ lưu URL thay vì key
    private String ensureKey(String maybeKeyOrUrl) {
        if (maybeKeyOrUrl == null) return null;
        if (!maybeKeyOrUrl.startsWith("http")) return maybeKeyOrUrl; // đã là key
        // URL dạng: http(s)://host:9000/<bucket>/<key>?...
        try {
            var u = java.net.URI.create(maybeKeyOrUrl);
            var path = u.getPath();          // /vocalearn/<key>
            if (path == null) return maybeKeyOrUrl;
            var p = path.startsWith("/") ? path.substring(1) : path;
            var idx = p.indexOf('/');
            if (idx < 0) return maybeKeyOrUrl;
            var bucketInUrl = p.substring(0, idx);
            var key = p.substring(idx + 1);
            // Nếu bucket trong URL trùng bucket cấu hình thì trả về key
            return bucketInUrl.equals(minIOProperties.getBucket()) ? key : maybeKeyOrUrl;
        } catch (Exception e) {
            return maybeKeyOrUrl;
        }
    }

    @CacheEvict(value = "presignedUrls", key = "#maybeKeyOrUrl")
    public void deleteFile(String maybeKeyOrUrl) {
        if (maybeKeyOrUrl == null || maybeKeyOrUrl.isBlank()) return;

        String key = maybeKeyOrUrl;

        // Nếu là URL, parse ra key
        if (key.startsWith("http")) {
            try {
                var uri = java.net.URI.create(key);
                String path = uri.getPath(); // /bucket-name/key
                if (path.startsWith("/")) path = path.substring(1);
                int idx = path.indexOf('/');
                if (idx >= 0) {
                    String bucketInUrl = path.substring(0, idx);
                    key = path.substring(idx + 1);
                    // Chỉ báo nếu bucket khác, nhưng không throw để stop
                    if (!bucketInUrl.equals(minIOProperties.getBucket())) {
                        System.err.println("⚠ Bucket trong URL không khớp với config: " + bucketInUrl);
                    }
                }
                key = java.net.URLDecoder.decode(key, StandardCharsets.UTF_8);
            } catch (Exception e) {
                System.err.println("⚠ Cannot parse S3 key from URL: " + key + " | " + e.getMessage());
                return; // Không throw, chỉ log
            }
        }

        // Thay space bằng '_' (giống khi upload)
        key = key.replace(" ", "_");

        try {
            var request = DeleteObjectRequest.builder()
                    .bucket(minIOProperties.getBucket())
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
        } catch (S3Exception e) {
            System.err.println("❌ Cannot delete S3 object: " + key);
            System.err.println("   StatusCode: " + e.statusCode());
            if (e.awsErrorDetails() != null) {
                System.err.println("   Message: " + e.awsErrorDetails().errorMessage());
            }
        }
    }


    //    public void copyFile(String sourceKey) {
//        var copySource = minIOProperties.getBucket() + "/" + sourceKey;
//        var destinationFileName = generateUniqueFileName(sourceKey);
//        var request = CopyObjectRequest.builder()
//                .copySource(copySource)
//                .destinationBucket(minIOProperties.getBucket())
//                .destinationKey(destinationFileName)
//                .build();
//        s3Client.copyObject(request);
//    }
    public String copyFile(String sourceKey) {
        if (sourceKey == null || sourceKey.isBlank()) return null;

        String copySource = minIOProperties.getBucket() + "/" + sourceKey;
        String destinationFileName = generateUniqueFileName(sourceKey);

        CopyObjectRequest request = CopyObjectRequest.builder()
                .copySource(copySource)
                .destinationBucket(minIOProperties.getBucket())
                .destinationKey(destinationFileName)
                .build();

        s3Client.copyObject(request);

        return destinationFileName; // trả về key mới
    }
    private String getFileExtension(String fileName) {
        if (fileName == null) return null;
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) return "";
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    @Transactional
    public void deleteQuestionMedia(ToeicQuestion question) {
        if (question.getImages() != null) {
            for (ToeicQuestionImage img : question.getImages()) {
                if (img.getUrl() != null) {
                    deleteFile(img.getUrl());
                }
            }
        }
        if (question.getAudio() != null) deleteFile(question.getAudio());
    }
}
