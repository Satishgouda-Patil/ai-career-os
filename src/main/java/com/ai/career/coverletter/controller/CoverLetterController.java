package com.ai.career.coverletter.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.coverletter.dto.CoverLetterResponse;
import com.ai.career.coverletter.dto.GenerateCoverLetterRequest;
import com.ai.career.coverletter.service.CoverLetterService;
import com.ai.career.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cover-letter")
@RequiredArgsConstructor
@Tag(name = "Cover Letter Intelligence", description = "Personalized, Role & Tone-Aware Cover Letter Generation APIs")
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    @PostMapping("/generate")
    @Operation(summary = "Generate cover letter", description = "Generates a personalized cover letter tailored to candidate profile and target job description")
    public ResponseEntity<ApiResponse<CoverLetterResponse>> generateCoverLetter(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @Valid @RequestBody GenerateCoverLetterRequest request
    ) {
        CoverLetterResponse response = coverLetterService.generateCoverLetter(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(response, "Cover letter generation completed"));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get cover letter for job", description = "Returns the latest generated cover letter for target job")
    public ResponseEntity<ApiResponse<CoverLetterResponse>> getCoverLetterByJobId(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long jobId
    ) {
        CoverLetterResponse response = coverLetterService.getLatestCoverLetterByJobId(userPrincipal.getId(), jobId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{jobId}/regenerate")
    @Operation(summary = "Regenerate cover letter", description = "Creates a new cover letter version for specified job")
    public ResponseEntity<ApiResponse<CoverLetterResponse>> regenerateCoverLetter(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long jobId
    ) {
        CoverLetterResponse response = coverLetterService.regenerateCoverLetter(userPrincipal.getId(), jobId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(response, "New cover letter version generated"));
    }

    @GetMapping("/history")
    @Operation(summary = "Get cover letter history", description = "Returns all cover letter generations for authenticated user")
    public ResponseEntity<ApiResponse<List<CoverLetterResponse>>> getCoverLetterHistory(
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<CoverLetterResponse> history = coverLetterService.getCoverLetterHistory(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
