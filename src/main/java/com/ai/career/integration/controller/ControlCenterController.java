package com.ai.career.integration.controller;

import com.ai.career.integration.dto.ControlCenterSummaryDto;
import com.ai.career.integration.service.ControlCenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/control-center")
@RequiredArgsConstructor
public class ControlCenterController {

    private final ControlCenterService controlCenterService;

    @GetMapping("/summary")
    public ResponseEntity<ControlCenterSummaryDto> getSummary(@RequestParam(value = "userId", defaultValue = "1") Long userId) {
        log.info("Fetching Production Control Center summary for user ID: {}", userId);
        ControlCenterSummaryDto summary = controlCenterService.getSummary(userId);
        return ResponseEntity.ok(summary);
    }
}
