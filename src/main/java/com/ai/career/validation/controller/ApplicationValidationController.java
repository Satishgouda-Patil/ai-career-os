package com.ai.career.validation.controller;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.common.dto.ApiResponse;
import com.ai.career.form.model.ApplicationFormPlan;
import com.ai.career.form.service.ApplicationFormService;
import com.ai.career.security.UserPrincipal;
import com.ai.career.validation.model.*;
import com.ai.career.validation.service.ApplicationDryRunService;
import com.ai.career.validation.service.ExecutionAuthorizationService;
import com.ai.career.validation.service.FormValidationEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}")
@RequiredArgsConstructor
@Tag(name = "Application Validation & Dry Run", description = "Validation Engine, Zero-Side-Effect Dry Run & Execution Authorization APIs")
public class ApplicationValidationController {

    private final ApplicationRepository applicationRepository;
    private final ApplicationFormService formService;
    private final FormValidationEngine validationEngine;
    private final ApplicationDryRunService dryRunService;
    private final ExecutionAuthorizationService authorizationService;

    @PostMapping("/validate")
    @Operation(summary = "Validate application", description = "Executes deterministic validation rules against candidate data and form fields")
    public ResponseEntity<ApiResponse<ApplicationValidationResult>> validateApplication(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId
    ) {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!app.getUser().getId().equals(userPrincipal.getId())) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        ApplicationFormPlan plan = formService.getFormPlan(userPrincipal.getId(), applicationId);
        ApplicationValidationResult result = validationEngine.validateApplication(app, plan);

        return ResponseEntity.ok(ApiResponse.success(result, "Application validation complete"));
    }

    @GetMapping("/validation")
    @Operation(summary = "Get application validation status", description = "Returns current validation result and machine-readable error reasons")
    public ResponseEntity<ApiResponse<ApplicationValidationResult>> getValidation(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId
    ) {
        return validateApplication(userPrincipal, applicationId);
    }

    @PostMapping("/dry-run")
    @Operation(summary = "Execute application dry run", description = "Simulates execution locally with ZERO external side effects")
    public ResponseEntity<ApiResponse<DryRunReport>> executeDryRun(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId
    ) {
        DryRunReport report = dryRunService.executeDryRun(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(report, "Application dry run complete"));
    }

    @GetMapping("/dry-run/{runId}")
    @Operation(summary = "Get dry run report", description = "Fetches persisted dry run report by run ID")
    public ResponseEntity<ApiResponse<DryRunReport>> getDryRunReport(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId,
        @PathVariable String runId
    ) {
        DryRunReport report = dryRunService.getDryRunReport(userPrincipal.getId(), runId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/readiness")
    @Operation(summary = "Get execution readiness status", description = "Returns final execution readiness decision and explainable reasons")
    public ResponseEntity<ApiResponse<ExecutionReadiness>> getReadiness(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId
    ) {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!app.getUser().getId().equals(userPrincipal.getId())) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        ApplicationFormPlan plan = formService.getFormPlan(userPrincipal.getId(), applicationId);
        ApplicationValidationResult result = validationEngine.validateApplication(app, plan);

        return ResponseEntity.ok(ApiResponse.success(result.getReadiness()));
    }

    @PostMapping("/authorize-execution")
    @Operation(summary = "Authorize execution", description = "Explicitly grants candidate authorization for application execution")
    public ResponseEntity<ApiResponse<ExecutionAuthorization>> authorizeExecution(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long applicationId,
        @RequestParam(required = false) String note
    ) {
        ExecutionAuthorization auth = authorizationService.authorizeExecution(userPrincipal.getId(), applicationId, note);
        return ResponseEntity.ok(ApiResponse.success(auth, "Candidate execution authorization granted"));
    }
}
