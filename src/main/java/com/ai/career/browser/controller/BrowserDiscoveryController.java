package com.ai.career.browser.controller;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.browser.discovery.BrowserDiscoveryResult;
import com.ai.career.browser.discovery.BrowserFormDiscoveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BrowserDiscoveryController {

    private final BrowserFormDiscoveryService discoveryService;
    private final ApplicationRepository applicationRepository;

    @PostMapping("/browser/discover")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BrowserDiscoveryResult> discoverForm(@Valid @RequestBody DiscoverFormRequest request) {
        log.info("Received browser discovery request for URL: {}", request.getUrl());
        BrowserDiscoveryResult result = discoveryService.discoverForms(request.getUrl());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/applications/{id}/browser/discover")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BrowserDiscoveryResult> discoverFormForApplication(@PathVariable("id") Long applicationId) {
        log.info("Received browser discovery request for Application ID: {}", applicationId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        String targetUrl = application.getJob() != null ? application.getJob().getUrl() : null;
        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Application does not have a valid job URL");
        }

        BrowserDiscoveryResult result = discoveryService.discoverForms(targetUrl);
        return ResponseEntity.ok(result);
    }
}
