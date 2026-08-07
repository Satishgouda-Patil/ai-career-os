package com.ai.career.storage.service.impl;

import com.ai.career.profile.service.ProfileService;
import com.ai.career.storage.service.FileStorageService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final ProfileService profileService;

    @Value("${app.minio.bucket-name:resumes}")
    private String bucketName;

    @Value("${app.minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Override
    public String uploadResume(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String originalFilename = file.getOriginalFilename();
            String objectName = "user_" + userId + "_" + System.currentTimeMillis() + "_" + originalFilename;

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
                );
            }

            String fileUrl = endpoint + "/" + bucketName + "/" + objectName;
            profileService.updateResumeUrl(userId, fileUrl);

            return fileUrl;
        } catch (Exception e) {
            log.error("Failed to upload resume to MinIO for user ID: {}", userId, e);
            throw new RuntimeException("Could not upload file to storage: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType, long size) {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build()
            );

            return endpoint + "/" + bucketName + "/" + fileName;
        } catch (Exception e) {
            log.error("Failed to upload file {} to MinIO: {}", fileName, e.getMessage());
            return endpoint + "/" + bucketName + "/" + fileName;
        }
    }
}
