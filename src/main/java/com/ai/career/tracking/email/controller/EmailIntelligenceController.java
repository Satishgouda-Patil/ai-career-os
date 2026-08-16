package com.ai.career.tracking.email.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.security.UserPrincipal;
import com.ai.career.tracking.email.dto.EmailMessageDto;
import com.ai.career.tracking.email.dto.SimulateEmailRequest;
import com.ai.career.tracking.email.service.EmailIngestionPipelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/email-intelligence")
@RequiredArgsConstructor
public class EmailIntelligenceController {

    private final EmailIngestionPipelineService pipelineService;

    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse<List<EmailMessageDto>>> ingestUserEmails(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "provider", defaultValue = "SIMULATED") String provider) {
        List<EmailMessageDto> ingested = pipelineService.ingestUserEmails(userPrincipal.getId(), provider);
        return ResponseEntity.ok(ApiResponse.success(ingested, "Emails ingested successfully"));
    }

    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<EmailMessageDto>> simulateEmail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody SimulateEmailRequest request) {
        EmailMessageDto message = pipelineService.ingestSimulatedEmail(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(message, "Simulated email ingested and processed successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmailMessageDto>>> getUserEmails(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<EmailMessageDto> emails = pipelineService.getUserEmails(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(emails, "User emails retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmailMessageDto>> getEmailDetails(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long emailId) {
        EmailMessageDto email = pipelineService.getEmailDetails(userPrincipal.getId(), emailId);
        return ResponseEntity.ok(ApiResponse.success(email, "Email details retrieved successfully"));
    }

    @PostMapping("/{id}/match")
    public ResponseEntity<ApiResponse<EmailMessageDto>> manuallyMatchApplication(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long emailId,
            @RequestBody Map<String, Long> request) {
        Long applicationId = request.get("applicationId");
        if (applicationId == null) {
            throw new IllegalArgumentException("applicationId must be specified in request body");
        }
        EmailMessageDto email = pipelineService.manuallyMatchApplication(userPrincipal.getId(), emailId, applicationId);
        return ResponseEntity.ok(ApiResponse.success(email, "Email matched to application successfully"));
    }
}
