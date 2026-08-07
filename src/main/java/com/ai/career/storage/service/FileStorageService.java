package com.ai.career.storage.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadResume(Long userId, MultipartFile file);
}
