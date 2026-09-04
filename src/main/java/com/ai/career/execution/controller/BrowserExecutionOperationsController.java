package com.ai.career.execution.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.dto.*;
import com.ai.career.execution.service.BrowserExecutionOperationsService;
import com.ai.career.execution.service.ExecutionSafetyConfigurationService;
import com.ai.career.execution.service.ProviderHealthService;
import com.ai.career.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/browser/execution")
@RequiredArgsConstructor
@Tag(name = "Browser Execution Operations", description = "Controlled ATS Execution Operations & Production Safety Control Center APIs")
public class BrowserExecutionOperationsController {

    private final BrowserExecutionOperationsService operationsService;
    private final ExecutionSafetyConfigurationService safetyConfigService;
    private final ProviderHealthService providerHealthService;
    private final UserRepository userRepository;

    @GetMapping("/runs")
    @Operation(summary = "Get execution runs history", description = "Returns execution runs history for authenticated user")
    public ResponseEntity<ApiResponse<List<ExecutionOperationsRunDto>>> getExecutionRuns(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Long applicationId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String provider) {
        User user = getUser(userPrincipal);
        List<ExecutionOperationsRunDto> runs = operationsService.getExecutionRuns(user, applicationId, status, provider);
        return ResponseEntity.ok(ApiResponse.success(runs));
    }

    @GetMapping("/runs/{runId}")
    @Operation(summary = "Get execution run detail", description = "Returns sanitized execution evidence and detail for a run")
    public ResponseEntity<ApiResponse<ExecutionOperationsRunDto>> getExecutionRunDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long runId) {
        User user = getUser(userPrincipal);
        ExecutionOperationsRunDto detail = operationsService.getExecutionRunDetail(user, runId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @GetMapping("/runs/{runId}/retry-eligibility")
    @Operation(summary = "Get run retry eligibility", description = "Evaluates whether an execution run is eligible for explicit retry")
    public ResponseEntity<ApiResponse<RetryEligibilityResponse>> getRetryEligibility(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long runId) {
        User user = getUser(userPrincipal);
        RetryEligibilityResponse eligibility = operationsService.getRetryEligibility(user, runId);
        return ResponseEntity.ok(ApiResponse.success(eligibility));
    }

    @PostMapping("/runs/{runId}/retry")
    @Operation(summary = "Retry execution run", description = "Initiates an explicit controlled retry attempt if eligible")
    public ResponseEntity<ApiResponse<ExecutionOperationsRunDto>> retryExecution(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long runId) {
        User user = getUser(userPrincipal);
        ExecutionOperationsRunDto result = operationsService.retryExecution(user, runId);
        return ResponseEntity.ok(ApiResponse.success(result, "Execution retry processed"));
    }

    @GetMapping("/providers/health")
    @Operation(summary = "Get provider health status", description = "Returns health and capability metrics for all integrated execution providers")
    public ResponseEntity<ApiResponse<ProviderHealthResponse>> getProviderHealth(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ProviderHealthResponse health = providerHealthService.getProviderHealth();
        return ResponseEntity.ok(ApiResponse.success(health));
    }

    @GetMapping("/safety")
    @Operation(summary = "Get safety configuration", description = "Returns effective server safety flags and invariants")
    public ResponseEntity<ApiResponse<SafetyConfigurationResponse>> getSafetyConfiguration(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        SafetyConfigurationResponse config = safetyConfigService.getEffectiveSafetyConfiguration();
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get execution metrics", description = "Returns aggregate execution metrics for the operations center")
    public ResponseEntity<ApiResponse<ExecutionMetricsResponse>> getExecutionMetrics(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        ExecutionMetricsResponse metrics = operationsService.getExecutionMetrics(user);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    private User getUser(UserPrincipal userPrincipal) {
        if (userPrincipal == null || userPrincipal.getId() == null) {
            throw new SecurityException("Unauthenticated request");
        }
        return userRepository.findById(userPrincipal.getId())
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("candidate@ai-career.os")
                        .passwordHash("hashed")
                        .build()));
    }
}
