package com.ai.career.execution.sandbox.controller;

import com.ai.career.execution.sandbox.dto.SandboxExecutionResultDto;
import com.ai.career.execution.sandbox.service.SandboxExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class SandboxExecutionController {

    private final SandboxExecutionService sandboxExecutionService;

    @PostMapping("/{id}/browser/sandbox/execute")
    public ResponseEntity<SandboxExecutionResultDto> executeSandbox(
            @PathVariable("id") Long applicationId,
            @RequestParam(value = "userId", defaultValue = "1") Long userId) {

        log.info("REST endpoint called: Execute sandbox workflow for app ID: {}, user ID: {}", applicationId, userId);
        SandboxExecutionResultDto result = sandboxExecutionService.executeSandbox(applicationId, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/browser/sandbox/status")
    public ResponseEntity<SandboxExecutionResultDto> getSandboxStatus(
            @PathVariable("id") Long applicationId,
            @RequestParam(value = "userId", defaultValue = "1") Long userId) {

        log.info("REST endpoint called: Get sandbox status for app ID: {}, user ID: {}", applicationId, userId);
        SandboxExecutionResultDto result = sandboxExecutionService.getLatestSandboxStatus(applicationId, userId);
        return ResponseEntity.ok(result);
    }
}
