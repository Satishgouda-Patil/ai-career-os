package com.ai.career.pipeline.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.pipeline.dto.PipelineStatusDto;
import com.ai.career.pipeline.service.PipelineOrchestratorService;
import com.ai.career.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pipeline")
@RequiredArgsConstructor
@Tag(name = "Pipeline Domain", description = "End-to-End Job Search Workflow Pipeline APIs")
public class PipelineController {

    private final PipelineOrchestratorService pipelineOrchestratorService;

    @PostMapping("/trigger")
    @Operation(summary = "Trigger end-to-end pipeline", description = "Executes the 15-stage job search workflow from discovery to interview prep")
    public ResponseEntity<ApiResponse<PipelineStatusDto>> triggerPipeline(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Long jobId) {
        PipelineStatusDto status = pipelineOrchestratorService.triggerEndToEndPipeline(userPrincipal.getId(), jobId);
        return ResponseEntity.ok(ApiResponse.success(status, "End-to-End Pipeline executed successfully across all 15 stages"));
    }

    @GetMapping("/status/{applicationId}")
    @Operation(summary = "Get pipeline status", description = "Monitors 15-stage workflow pipeline progress and logs")
    public ResponseEntity<ApiResponse<PipelineStatusDto>> getPipelineStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long applicationId) {
        PipelineStatusDto status = pipelineOrchestratorService.getPipelineStatus(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(status, "Pipeline status retrieved"));
    }
}
