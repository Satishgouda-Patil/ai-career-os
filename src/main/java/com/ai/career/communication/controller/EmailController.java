package com.ai.career.communication.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.communication.dto.EmailResponse;
import com.ai.career.communication.dto.GenerateEmailRequest;
import com.ai.career.communication.service.EmailGeneratorService;
import com.ai.career.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
@Tag(name = "Communication Intelligence", description = "AI Cold Email, Follow-Up & LinkedIn Connection Messaging APIs")
public class EmailController {

    private final EmailGeneratorService emailGeneratorService;

    @PostMapping("/generate")
    @Operation(summary = "Generate cold email draft", description = "Generates personalized cold email, follow-up note, and LinkedIn message using AI Orchestrator")
    public ResponseEntity<ApiResponse<EmailResponse>> generateEmail(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @Valid @RequestBody GenerateEmailRequest request
    ) {
        EmailResponse response = emailGeneratorService.generateColdEmail(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(response, "Cold email draft generated"));
    }

    @GetMapping("/{emailId}")
    @Operation(summary = "Get email draft details", description = "Returns details of a generated email draft")
    public ResponseEntity<ApiResponse<EmailResponse>> getEmailById(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long emailId
    ) {
        EmailResponse response = emailGeneratorService.getEmailById(userPrincipal.getId(), emailId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{emailId}/regenerate")
    @Operation(summary = "Regenerate email draft", description = "Creates a new email draft version")
    public ResponseEntity<ApiResponse<EmailResponse>> regenerateEmail(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long emailId
    ) {
        EmailResponse response = emailGeneratorService.regenerateEmail(userPrincipal.getId(), emailId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(response, "New email draft version generated"));
    }

    @GetMapping("/history")
    @Operation(summary = "Get email history", description = "Returns all cold email drafts for authenticated user")
    public ResponseEntity<ApiResponse<List<EmailResponse>>> getEmailHistory(
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<EmailResponse> history = emailGeneratorService.getEmailHistory(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
