package com.ai.career.storage.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadResume(Long userId, MultipartFile file);
    String uploadFile(java.io.InputStream inputStream, String fileName, String contentType, long size);
}
