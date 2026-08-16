package com.ai.career.tracking.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.security.UserPrincipal;
import com.ai.career.tracking.dto.ActivityDto;
import com.ai.career.tracking.dto.ApplicationTrackingSummary;
import com.ai.career.tracking.dto.RecordActivityRequest;
import com.ai.career.tracking.service.ApplicationTimelineService;
import com.ai.career.tracking.service.ApplicationTrackingService;
import com.ai.career.tracking.service.NextActionDecision;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationTrackingController {

    private final ApplicationTrackingService trackingService;
    private final ApplicationTimelineService timelineService;

    @GetMapping("/{id}/tracking")
    public ResponseEntity<ApiResponse<ApplicationTrackingSummary>> getTrackingSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long applicationId) {
        ApplicationTrackingSummary summary = trackingService.getTrackingSummary(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Tracking summary retrieved successfully"));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<ApiResponse<List<ActivityDto>>> getTimeline(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long applicationId) {
        List<ActivityDto> timeline = timelineService.getTimeline(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(timeline, "Timeline activities retrieved successfully"));
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ApiResponse<ActivityDto>> recordActivity(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long applicationId,
            @RequestBody RecordActivityRequest request) {
        ActivityDto activity = timelineService.recordActivity(userPrincipal.getId(), applicationId, request);
        return ResponseEntity.ok(ApiResponse.success(activity, "Activity recorded successfully"));
    }

    @GetMapping("/{id}/next-action")
    public ResponseEntity<ApiResponse<NextActionDecision>> getNextAction(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long applicationId) {
        NextActionDecision nextAction = trackingService.getNextAction(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(nextAction, "Next action decision retrieved successfully"));
    }
}
