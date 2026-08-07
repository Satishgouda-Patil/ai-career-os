package com.ai.career.workspace.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.security.UserPrincipal;
import com.ai.career.workspace.dto.WorkspaceResponse;
import com.ai.career.workspace.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "AI Workspace", description = "Unified AI Workspace Aggregator, Review & Approval Workflow APIs")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping("/workspace/{jobId}")
    @Operation(summary = "Build AI workspace", description = "Builds full AI workspace combining Resume, ATS Analysis, Cover Letter, Recruiter Info, and Cold Email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> buildWorkspace(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long jobId
    ) {
        WorkspaceResponse workspace = workspaceService.buildWorkspace(userPrincipal.getId(), jobId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(Map.of("workspaceId", workspace.getWorkspaceId(), "status", workspace.getStatus())));
    }

    @PostMapping("/ai/workspace/{jobId}")
    @Operation(summary = "Generate Every AI Artifact for Job", description = "Trigger generation of all AI artifacts for target job")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateAiWorkspace(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long jobId
    ) {
        workspaceService.buildWorkspace(userPrincipal.getId(), jobId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "PROCESSING")));
    }

    @GetMapping("/workspace/{jobId}")
    @Operation(summary = "Get AI workspace", description = "Returns aggregated AI workspace containing all candidate application assets for target job")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspace(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long jobId
    ) {
        WorkspaceResponse workspace = workspaceService.getWorkspace(userPrincipal.getId(), jobId);
        return ResponseEntity.ok(ApiResponse.success(workspace));
    }

    @PostMapping("/workspace/{jobId}/approve")
    @Operation(summary = "Approve workspace assets", description = "Marks candidate application assets as approved for target job")
    public ResponseEntity<ApiResponse<Map<String, String>>> approveWorkspace(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long jobId
    ) {
        workspaceService.approveWorkspace(userPrincipal.getId(), jobId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "APPROVED")));
    }

    @PostMapping("/workspace/{jobId}/reject")
    @Operation(summary = "Reject workspace assets", description = "Marks workspace assets as rejected")
    public ResponseEntity<ApiResponse<Map<String, String>>> rejectWorkspace(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long jobId
    ) {
        workspaceService.rejectWorkspace(userPrincipal.getId(), jobId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "REJECTED")));
    }

    @PostMapping("/workspace/{jobId}/regenerate")
    @Operation(summary = "Regenerate workspace assets", description = "Re-runs full AI pipeline creating fresh candidate assets")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> regenerateWorkspace(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long jobId
    ) {
        WorkspaceResponse workspace = workspaceService.regenerateWorkspace(userPrincipal.getId(), jobId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(workspace, "Fresh AI workspace generated"));
    }
}
