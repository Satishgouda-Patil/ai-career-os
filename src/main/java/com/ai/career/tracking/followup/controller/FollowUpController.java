package com.ai.career.tracking.followup.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.security.UserPrincipal;
import com.ai.career.tracking.followup.dto.FollowUpDto;
import com.ai.career.tracking.followup.dto.GenerateFollowUpRequest;
import com.ai.career.tracking.followup.service.FollowUpGeneratorService;
import com.ai.career.tracking.followup.service.FollowUpSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpSchedulerService schedulerService;
    private final FollowUpGeneratorService generatorService;

    @GetMapping("/follow-ups")
    public ResponseEntity<ApiResponse<List<FollowUpDto>>> getUserFollowUps(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<FollowUpDto> followUps = schedulerService.getUserFollowUps(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(followUps, "User follow-ups retrieved successfully"));
    }

    @GetMapping("/applications/{id}/follow-ups")
    public ResponseEntity<ApiResponse<List<FollowUpDto>>> getApplicationFollowUps(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long applicationId) {
        List<FollowUpDto> followUps = schedulerService.getApplicationFollowUps(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(followUps, "Application follow-ups retrieved successfully"));
    }

    @PostMapping("/applications/{id}/follow-ups/generate")
    public ResponseEntity<ApiResponse<FollowUpDto>> generateFollowUpDraft(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long applicationId,
            @RequestBody(required = false) GenerateFollowUpRequest request) {
        Integer sequenceNumber = request != null ? request.getSequenceNumber() : 1;
        String customNotes = request != null ? request.getCustomNotes() : null;
        FollowUpDto draft = generatorService.generateFollowUpDraft(userPrincipal.getId(), applicationId, sequenceNumber, customNotes);
        return ResponseEntity.ok(ApiResponse.success(draft, "Follow-up draft generated successfully"));
    }

    @PostMapping("/follow-ups/{id}/approve")
    public ResponseEntity<ApiResponse<FollowUpDto>> approveFollowUp(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long followUpId) {
        FollowUpDto followUp = schedulerService.approveFollowUp(userPrincipal.getId(), followUpId);
        return ResponseEntity.ok(ApiResponse.success(followUp, "Follow-up approved successfully"));
    }

    @PostMapping("/follow-ups/{id}/send")
    public ResponseEntity<ApiResponse<FollowUpDto>> sendFollowUp(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long followUpId) {
        FollowUpDto followUp = schedulerService.sendFollowUp(userPrincipal.getId(), followUpId);
        return ResponseEntity.ok(ApiResponse.success(followUp, "Follow-up dispatched successfully"));
    }

    @PostMapping("/follow-ups/{id}/cancel")
    public ResponseEntity<ApiResponse<FollowUpDto>> cancelFollowUp(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long followUpId,
            @RequestBody(required = false) Map<String, String> request) {
        String reason = (request != null && request.containsKey("reason")) ? request.get("reason") : "Cancelled by user";
        FollowUpDto followUp = schedulerService.cancelFollowUp(userPrincipal.getId(), followUpId, reason);
        return ResponseEntity.ok(ApiResponse.success(followUp, "Follow-up cancelled successfully"));
    }
}
