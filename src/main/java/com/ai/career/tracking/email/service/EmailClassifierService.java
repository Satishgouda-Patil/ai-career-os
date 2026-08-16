package com.ai.career.tracking.email.service;

import com.ai.career.tracking.email.dto.EmailClassificationDto;

public interface EmailClassifierService {
    EmailClassificationDto classify(String sender, String subject, String bodySnippet);
}
