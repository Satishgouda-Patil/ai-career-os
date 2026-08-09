package com.ai.career.application.controller;

import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.dto.ApplicationHistoryResponse;
import com.ai.career.application.dto.ApplicationResponse;
import com.ai.career.application.dto.CreateApplicationRequest;
import com.ai.career.application.dto.TransitionStateRequest;
import com.ai.career.application.service.ApplicationService;
import com.ai.career.common.dto.ApiResponse;
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
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Application Domain", description = "Application Domain & Lifecycle State Machine APIs")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @Operation(summary = "Create application", description = "Initializes a new candidate application domain object")
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @Valid @RequestBody CreateApplicationRequest request
    ) {
        ApplicationResponse response = applicationService.createApplication(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Application initialized"));
    }

    @GetMapping
    @Operation(summary = "Get user applications", description = "Returns candidate applications with optional status filter")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getUserApplications(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @RequestParam(required = false) ApplicationState status
    ) {
        List<ApplicationResponse> applications = applicationService.getUserApplications(userPrincipal.getId(), status);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get application details", description = "Returns details and history of an application")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId
    ) {
        ApplicationResponse response = applicationService.getApplicationById(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{applicationId}/transition")
    @Operation(summary = "Transition application state", description = "Validates and executes a formal lifecycle state transition")
    public ResponseEntity<ApiResponse<ApplicationResponse>> transitionState(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId,
        @Valid @RequestBody TransitionStateRequest request
    ) {
        ApplicationResponse response = applicationService.transitionState(userPrincipal.getId(), applicationId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Application state transitioned to " + response.getStatus()));
    }

    @GetMapping("/{applicationId}/history")
    @Operation(summary = "Get application history", description = "Returns audit history of state transitions")
    public ResponseEntity<ApiResponse<List<ApplicationHistoryResponse>>> getApplicationHistory(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId
    ) {
        List<ApplicationHistoryResponse> history = applicationService.getApplicationHistory(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
