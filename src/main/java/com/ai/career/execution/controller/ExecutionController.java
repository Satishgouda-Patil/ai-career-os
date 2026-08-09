package com.ai.career.execution.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.execution.dto.ExecuteApplicationRequest;
import com.ai.career.execution.dto.ExecutionResponse;
import com.ai.career.execution.provider.ExecutionValidationResult;
import com.ai.career.execution.service.ApplicationExecutionService;
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
@Tag(name = "Application Execution", description = "Application Execution Engine & Provider SPI APIs")
public class ExecutionController {

    private final ApplicationExecutionService executionService;

    @PostMapping("/execute")
    @Operation(summary = "Execute application", description = "Executes application via resolved execution provider")
    public ResponseEntity<ApiResponse<ExecutionResponse>> executeApplication(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId,
        @RequestBody(required = false) ExecuteApplicationRequest request
    ) {
        ExecutionResponse response = executionService.executeApplication(userPrincipal.getId(), applicationId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Application execution processed"));
    }

    @PostMapping("/execute/validate")
    @Operation(summary = "Validate application execution", description = "Validates application form data prior to execution")
    public ResponseEntity<ApiResponse<ExecutionValidationResult>> validateExecution(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId,
        @RequestBody(required = false) ExecuteApplicationRequest request
    ) {
        ExecutionValidationResult validation = executionService.validateExecution(userPrincipal.getId(), applicationId, request);
        return ResponseEntity.ok(ApiResponse.success(validation));
    }

    @GetMapping("/executions")
    @Operation(summary = "Get application executions", description = "Returns execution history for an application")
    public ResponseEntity<ApiResponse<List<ExecutionResponse>>> getExecutions(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId
    ) {
        List<ExecutionResponse> executions = executionService.getExecutionsByApplicationId(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(executions));
    }

    @GetMapping("/executions/{executionId}")
    @Operation(summary = "Get execution details", description = "Returns single execution details")
    public ResponseEntity<ApiResponse<ExecutionResponse>> getExecutionById(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId,
        @PathVariable Long executionId
    ) {
        ExecutionResponse response = executionService.getExecutionById(userPrincipal.getId(), applicationId, executionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
