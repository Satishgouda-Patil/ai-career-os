package com.ai.career.execution.controller;

import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.dto.*;
import com.ai.career.execution.service.ControlledApplicationExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/applications/{applicationId}/browser")
@RequiredArgsConstructor
public class ControlledApplicationExecutionController {

    private final ControlledApplicationExecutionService executionService;
    private final UserRepository userRepository;

    @PostMapping("/execute/start")
    public ResponseEntity<ControlledExecutionStepResponse> startExecution(
            @PathVariable Long applicationId,
            @RequestBody(required = false) ControlledExecutionStartRequest request) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(executionService.startExecution(user, applicationId, request));
    }

    @PostMapping("/execute/inspect")
    public ResponseEntity<ControlledExecutionStepResponse> inspectApplication(
            @PathVariable Long applicationId) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(executionService.inspectApplication(user, applicationId));
    }

    @PostMapping("/execute/map")
    public ResponseEntity<ControlledExecutionStepResponse> mapCandidateFields(
            @PathVariable Long applicationId) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(executionService.mapCandidateFields(user, applicationId));
    }

    @PostMapping("/execute/sandbox")
    public ResponseEntity<ControlledExecutionStepResponse> runSandboxVerification(
            @PathVariable Long applicationId) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(executionService.runSandboxVerification(user, applicationId));
    }

    @PostMapping("/execute/prepare")
    public ResponseEntity<ControlledExecutionStepResponse> prepareProductionExecution(
            @PathVariable Long applicationId) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(executionService.prepareProductionExecution(user, applicationId));
    }

    @GetMapping("/execute/review")
    public ResponseEntity<ControlledExecutionSafetyReviewResponse> getSafetyReview(
            @PathVariable Long applicationId) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(executionService.getSafetyReview(user, applicationId));
    }

    @PostMapping("/execute/confirm")
    public ResponseEntity<ControlledExecutionStepResponse> confirmCandidate(
            @PathVariable Long applicationId,
            @RequestBody ControlledExecutionConfirmRequest request) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(executionService.confirmCandidate(user, applicationId, request));
    }

    @PostMapping("/execute/run")
    public ResponseEntity<ControlledExecutionStepResponse> runExecution(
            @PathVariable Long applicationId,
            @RequestBody(required = false) ControlledExecutionRunRequest request) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(executionService.execute(user, applicationId, request));
    }

    @PostMapping("/execute/verify")
    public ResponseEntity<ControlledExecutionStepResponse> verifyExecution(
            @PathVariable Long applicationId) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(executionService.verify(user, applicationId));
    }

    @GetMapping("/execute/status")
    public ResponseEntity<ControlledExecutionStatusResponse> getExecuteStatus(
            @PathVariable Long applicationId) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(executionService.getStatus(user, applicationId));
    }

    @GetMapping("/execution/status")
    public ResponseEntity<ControlledExecutionStatusResponse> getExecutionStatus(
            @PathVariable Long applicationId) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(executionService.getStatus(user, applicationId));
    }

    @GetMapping("/execute/evidence")
    public ResponseEntity<Map<String, Object>> getExecutionEvidence(
            @PathVariable Long applicationId) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(executionService.getEvidence(user, applicationId));
    }

    @PostMapping("/execute")
    public ResponseEntity<ControlledExecutionStepResponse> executeUnifiedPipeline(
            @PathVariable Long applicationId,
            @RequestBody(required = false) ControlledExecutionRunRequest request) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(executionService.runFullPipeline(user, applicationId, request));
    }

    private User getAuthenticatedUser() {
        return userRepository.findAll().stream().findFirst()
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("candidate@ai-career.os")
                        .passwordHash("hashed")
                        .build()));
    }
}
