package com.ai.career.tracking.email.service;

import com.ai.career.tracking.email.dto.EmailMessageDto;
import com.ai.career.tracking.email.dto.SimulateEmailRequest;

import java.util.List;

public interface EmailIngestionPipelineService {
    List<EmailMessageDto> ingestUserEmails(Long userId, String providerName);
    EmailMessageDto ingestSimulatedEmail(Long userId, SimulateEmailRequest request);
    List<EmailMessageDto> getUserEmails(Long userId);
    EmailMessageDto getEmailDetails(Long userId, Long emailId);
    EmailMessageDto manuallyMatchApplication(Long userId, Long emailId, Long applicationId);
}
