package com.ai.career.browser.controller;

import com.ai.career.browser.interaction.BrowserInteractionPlan;
import com.ai.career.browser.interaction.BrowserInteractionService;
import com.ai.career.browser.interaction.SubmissionPreview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/applications/{id}/browser")
@RequiredArgsConstructor
public class BrowserInteractionController {

    private final BrowserInteractionService interactionService;

    @PostMapping("/prepare")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BrowserInteractionPlan> prepareInteraction(@PathVariable("id") Long applicationId) {
        log.info("Preparing browser interaction plan for application ID: {}", applicationId);
        BrowserInteractionPlan plan = interactionService.prepareInteractionPlan(applicationId);
        return ResponseEntity.ok(plan);
    }

    @PostMapping("/execute-interaction")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubmissionPreview> executeInteraction(@PathVariable("id") Long applicationId) {
        log.info("Executing controlled browser interaction for application ID: {}", applicationId);
        SubmissionPreview preview = interactionService.executeInteraction(applicationId);
        return ResponseEntity.ok(preview);
    }

    @GetMapping("/preview")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubmissionPreview> getPreview(@PathVariable("id") Long applicationId) {
        log.info("Fetching submission preview for application ID: {}", applicationId);
        SubmissionPreview preview = interactionService.executeInteraction(applicationId);
        return ResponseEntity.ok(preview);
    }
}
