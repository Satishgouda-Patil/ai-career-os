package com.ai.career.communication.service;

import com.ai.career.communication.dto.EmailResponse;
import com.ai.career.communication.dto.GenerateEmailRequest;

import java.util.List;

public interface EmailGeneratorService {
    EmailResponse generateColdEmail(Long userId, GenerateEmailRequest request);
    EmailResponse getEmailById(Long userId, Long emailId);
    EmailResponse getLatestEmailByJobId(Long userId, Long jobId);
    EmailResponse regenerateEmail(Long userId, Long emailId);
    List<EmailResponse> getEmailHistory(Long userId);
}
