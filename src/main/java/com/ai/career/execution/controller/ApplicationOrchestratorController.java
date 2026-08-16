package com.ai.career.execution.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.execution.dto.ApproveAndPrepareRequest;
import com.ai.career.execution.dto.ExecuteApplicationRequest;
import com.ai.career.execution.dto.WorkflowOrchestrationResponse;
import com.ai.career.execution.service.ApplicationOrchestratorService;
import com.ai.career.execution.service.ApplicationReadinessResult;
import com.ai.career.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}")
@RequiredArgsConstructor
@Tag(name = "Automation Orchestration", description = "Application Automation Orchestration & End-to-End Pipeline APIs")
public class ApplicationOrchestratorController {

    private final ApplicationOrchestratorService orchestratorService;

    @PostMapping("/orchestrate")
    @Operation(summary = "Orchestrate application workflow", description = "Runs end-to-end orchestration pipeline for an application idempotently")
    public ResponseEntity<ApiResponse<WorkflowOrchestrationResponse>> orchestrate(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long applicationId,
            @RequestBody(required = false) ExecuteApplicationRequest request
    ) {
        WorkflowOrchestrationResponse response = orchestratorService.orchestrate(userPrincipal.getId(), applicationId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Application orchestration executed"));
    }

    @PostMapping("/approve-and-prepare")
    @Operation(summary = "Approve and prepare application", description = "Records human approval and triggers orchestration pipeline")
    public ResponseEntity<ApiResponse<WorkflowOrchestrationResponse>> approveAndPrepare(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long applicationId,
            @RequestBody(required = false) ApproveAndPrepareRequest request
    ) {
        String approvedBy = request != null && request.getApprovedBy() != null ? request.getApprovedBy() : String.valueOf(userPrincipal.getId());
        String reason = request != null ? request.getReason() : "Approved by human user";
        WorkflowOrchestrationResponse response = orchestratorService.approveAndPrepare(userPrincipal.getId(), applicationId, approvedBy, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Application approved and prepared for execution"));
    }

    @GetMapping("/pipeline-readiness")
    @Operation(summary = "Get application readiness", description = "Evaluates application readiness and returns missing artifacts/unresolved fields")
    public ResponseEntity<ApiResponse<ApplicationReadinessResult>> getReadiness(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long applicationId
    ) {
        ApplicationReadinessResult readiness = orchestratorService.getReadiness(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(readiness));
    }

    @GetMapping("/workflow")
    @Operation(summary = "Get workflow run history", description = "Returns workflow orchestration history for an application")
    public ResponseEntity<ApiResponse<List<WorkflowOrchestrationResponse>>> getWorkflowHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long applicationId
    ) {
        List<WorkflowOrchestrationResponse> history = orchestratorService.getWorkflowHistory(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
