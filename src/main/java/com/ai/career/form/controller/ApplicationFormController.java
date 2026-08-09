package com.ai.career.form.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.form.model.ApplicationFormPlan;
import com.ai.career.form.service.ApplicationFormService;
import com.ai.career.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/form")
@RequiredArgsConstructor
@Tag(name = "Application Form Intelligence", description = "Form Discovery, Field Classification & Candidate Answer Mapping APIs")
public class ApplicationFormController {

    private final ApplicationFormService formService;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze application form", description = "Discovers, classifies, and maps candidate answers to form fields")
    public ResponseEntity<ApiResponse<ApplicationFormPlan>> analyzeForm(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId
    ) {
        ApplicationFormPlan plan = formService.analyzeApplicationForm(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(plan, "Application form analysis complete"));
    }

    @GetMapping("/plan")
    @Operation(summary = "Get application form plan", description = "Returns proposed field mappings and readiness status")
    public ResponseEntity<ApiResponse<ApplicationFormPlan>> getFormPlan(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId
    ) {
        ApplicationFormPlan plan = formService.getFormPlan(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    @PostMapping("/approve")
    @Operation(summary = "Approve form plan", description = "Approves candidate form plan and marks form plan as READY")
    public ResponseEntity<ApiResponse<ApplicationFormPlan>> approveFormPlan(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId
    ) {
        ApplicationFormPlan plan = formService.approveFormPlan(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(plan, "Application form plan approved"));
    }
}
