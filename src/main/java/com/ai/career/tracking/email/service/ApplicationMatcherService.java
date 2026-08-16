package com.ai.career.tracking.email.service;

import com.ai.career.application.domain.entity.Application;

import java.util.Optional;

public interface ApplicationMatcherService {
    Optional<ApplicationMatchResult> matchApplication(Long userId, String sender, String subject, String bodySnippet, String externalThreadId);

    record ApplicationMatchResult(Application application, double matchConfidence, String matchReason) {}
}
