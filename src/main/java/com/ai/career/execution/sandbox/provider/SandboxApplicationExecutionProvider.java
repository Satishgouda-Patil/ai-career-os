package com.ai.career.execution.sandbox.provider;

import com.ai.career.browser.interaction.BrowserInteractionService;
import com.ai.career.browser.interaction.SubmissionPreview;
import com.ai.career.execution.sandbox.dto.SandboxExecutionResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SandboxApplicationExecutionProvider {

    private final BrowserInteractionService interactionService;

    public SandboxExecutionResultDto executeSandboxApplication(Long applicationId, Long userId) {
        log.info("Executing controlled sandbox application simulation for app ID: {}, user ID: {}", applicationId, userId);
        LocalDateTime startTime = LocalDateTime.now();

        // Perform M6-B sandbox form interaction & preview verification
        SubmissionPreview preview = interactionService.executeInteraction(applicationId);

        boolean isVerified = "READY_FOR_REVIEW".equalsIgnoreCase(preview.getStatus())
                || "CONTROLLED_INTERACTION_VERIFIED_STOPPED".equalsIgnoreCase(preview.getStatus());

        return SandboxExecutionResultDto.builder()
                .applicationId(applicationId)
                .executionMode("SANDBOX")
                .status(isVerified ? "VERIFIED" : "FAILED")
                .fieldsDetected(preview.getFieldsDetected())
                .fieldsMapped(preview.getFieldsMapped())
                .fieldsVerified(preview.getFieldsMapped())
                .fieldsRequireReview(preview.getFieldsRequireReview())
                .submissionSimulated(true)
                .submissionVerified(isVerified)
                .realSubmissionAttempted(false)
                .emailSent(false)
                .fileUploadedToRealProvider(false)
                .startedAt(startTime)
                .completedAt(LocalDateTime.now())
                .errorCode(isVerified ? null : preview.getStatus())
                .build();
    }
}
