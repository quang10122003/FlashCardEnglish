package com.TestFlashCard.FlashCard.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
public class MinioBucketInitializer {

    private final S3Client s3Client;
    private final MinIOProperties minIOProperties;

    @PostConstruct
    public void ensureBucketExists() {
        String bucket = minIOProperties.getBucket();
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("MinIO bucket name must not be empty");
        }

        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            System.out.println("[MinIO] Created bucket: " + bucket);
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException e) {
            System.out.println("[MinIO] Bucket already exists: " + bucket);
        } catch (S3Exception e) {
            if (e.statusCode() == 409) {
                System.out.println("[MinIO] Bucket already exists or is in use: " + bucket);
            } else {
                throw e;
            }
        }
    }
}
