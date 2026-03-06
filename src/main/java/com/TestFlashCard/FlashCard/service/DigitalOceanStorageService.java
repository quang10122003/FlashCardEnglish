package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.TestFlashCard.FlashCard.exception.StorageException;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class DigitalOceanStorageService {
    @Value("${digitalocean.space.access-key}")
    private String accessKey;

    @Value("${digitalocean.space.secret-key}")
    private String secretKey;

    @Value("${digitalocean.space.endpoint}")
    private String endpoint;

    @Value("${digitalocean.space.name}")
    private String spaceName;

    @Value("${digitalocean.space.region}")
    private String region;

    public static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/octet-stream");

    private S3Client getS3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(region))
                .build();
    }

    public String uploadImage(String fileName, InputStream inputStream, String contentType) throws IOException {
        String uniqueFileName = generateUniqueFileName(fileName);

        S3Client s3Client = getS3Client();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(spaceName)
                .key(uniqueFileName)
                .contentType(contentType)
                .acl("public-read") // file public
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, inputStream.available()));
        return String.format("%s/%s/%s", endpoint, spaceName, uniqueFileName);
    }

    private String generateUniqueFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "-" + originalFileName;
    }

    public void deleteImage(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            S3Client s3Client = getS3Client();

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(spaceName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception exception) {
            throw new StorageException("Error when delete Image from DigitalOcean Space!", exception);
        }
    }

    private String extractKeyFromUrl(String fileUrl) {
        // endpoint sẽ không có dấu "/" cuối
        String base = endpoint.endsWith("/") ? endpoint : endpoint + "/";
        // base ví dụ: https://your-space.sgp1.digitaloceanspaces.com/
        if (fileUrl.startsWith(base)) {
            return fileUrl.substring(base.length());
        }
        // fallback: tìm đoạn sau endpoint
        int idx = fileUrl.indexOf(".digitaloceanspaces.com/");
        if (idx != -1) {
            return fileUrl.substring(idx + ".digitaloceanspaces.com/".length() + 1);
        }
        // fallback cuối cùng: chỉ lấy tên file
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }

    public String uploadAudio(String fileName, InputStream inputStream, String contentType) throws IOException {
        String uniqueFileName = generateUniqueFileName(fileName);

        S3Client s3Client = getS3Client();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(spaceName)
                .key(uniqueFileName)
                .contentType(contentType)
                .acl("public-read")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, inputStream.available()));

        return String.format("%s/%s/%s", endpoint, spaceName, uniqueFileName);
    }

    public void deleteAudio(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            S3Client s3Client = getS3Client();

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(spaceName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception exception) {
            throw new StorageException("Error when delete Audio from DigitalOcean Space!", exception);
        }
    }

    public String copyFile(String sourceFileUrl, String newFileName) {
        S3Client s3Client = getS3Client();
        String sourceKey = extractKeyFromUrl(sourceFileUrl);
        String destinationKey = generateUniqueFileName(newFileName);
        System.out.println("Copying file: " + sourceKey + " in bucket: " + spaceName);

        // Copy trong cùng 1 bucket (Spaces)
        CopyObjectRequest copyReq = CopyObjectRequest.builder()
                .copySource(spaceName + "/" + sourceKey)
                .destinationBucket(spaceName)
                .destinationKey(destinationKey)
                .acl("public-read")
                .build();

        s3Client.copyObject(copyReq);

        // Trả về url mới
        return String.format("%s/%s/%s", endpoint, spaceName, destinationKey);
    }
}
