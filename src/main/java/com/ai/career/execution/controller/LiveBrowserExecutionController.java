package com.ai.career.execution.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.dto.*;
import com.ai.career.execution.service.LiveBrowserExecutionService;
import com.ai.career.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/browser/live")
@RequiredArgsConstructor
@Tag(name = "Live Browser Execution", description = "Controlled Real ATS Execution & Final Submission Verification APIs")
public class LiveBrowserExecutionController {

    private final LiveBrowserExecutionService liveService;
    private final UserRepository userRepository;

    @PostMapping("/prepare")
    @Operation(summary = "Prepare live browser execution", description = "Discovers form and prepares read-only execution preview for Greenhouse")
    public ResponseEntity<ApiResponse<LiveExecutionPrepareResponse>> prepareLiveExecution(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long applicationId) {
        User user = getUser(userPrincipal);
        LiveExecutionPrepareResponse response = liveService.prepareLiveExecution(user, applicationId);
        return ResponseEntity.ok(ApiResponse.success(response, "Live execution prepared in PRODUCTION_READ_ONLY mode"));
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirm candidate submission", description = "Transitions application to CONFIRMED_SUBMISSION after explicit candidate review")
    public ResponseEntity<ApiResponse<LiveExecutionConfirmResponse>> confirmLiveExecution(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long applicationId,
            @RequestBody(required = false) LiveExecutionConfirmRequest request) {
        User user = getUser(userPrincipal);
        String fingerprint = request != null ? request.getFormFingerprint() : null;
        LiveExecutionConfirmResponse response = liveService.confirmLiveExecution(user, applicationId, fingerprint);
        return ResponseEntity.ok(ApiResponse.success(response, "Candidate submission confirmed"));
    }

    @PostMapping("/execute")
    @Operation(summary = "Execute live browser submission", description = "Executes real ATS submission if ALLOW_LIVE_SUBMISSION is enabled and all 17 safety checks pass")
    public ResponseEntity<ApiResponse<LiveExecutionExecuteResponse>> executeLiveSubmission(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long applicationId,
            @RequestBody(required = false) LiveExecutionExecuteRequest request) {
        User user = getUser(userPrincipal);
        String fingerprint = request != null ? request.getFormFingerprint() : null;
        LiveExecutionExecuteResponse response = liveService.executeLiveSubmission(user, applicationId, fingerprint);
        return ResponseEntity.ok(ApiResponse.success(response, "Live submission execution processed"));
    }

    @GetMapping("/status")
    @Operation(summary = "Get live browser execution status", description = "Returns live browser execution mode, status, and safety flags")
    public ResponseEntity<ApiResponse<LiveExecutionStatusResponse>> getLiveExecutionStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long applicationId) {
        User user = getUser(userPrincipal);
        LiveExecutionStatusResponse response = liveService.getLiveExecutionStatus(user, applicationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private User getUser(UserPrincipal userPrincipal) {
        if (userPrincipal == null || userPrincipal.getId() == null) {
            throw new SecurityException("Unauthenticated request");
        }
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userPrincipal.getId()));
    }
}
