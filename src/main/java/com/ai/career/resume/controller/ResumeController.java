package com.ai.career.resume.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.resume.dto.AtsAnalysisResponse;
import com.ai.career.resume.dto.GenerateResumeRequest;
import com.ai.career.resume.dto.ResumeResponse;
import com.ai.career.resume.service.AtsAnalysisService;
import com.ai.career.resume.service.ResumeService;
import com.ai.career.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resume")
@RequiredArgsConstructor
@Tag(name = "Resume Intelligence", description = "ATS Resume Generation, Scoring, Versioning & Export APIs")
public class ResumeController {

    private final ResumeService resumeService;
    private final AtsAnalysisService atsAnalysisService;

    @PostMapping("/generate")
    @Operation(summary = "Generate ATS optimized resume", description = "Generates a new targeted resume version for candidate using AI Orchestrator")
    public ResponseEntity<ApiResponse<ResumeResponse>> generateResume(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @Valid @RequestBody GenerateResumeRequest request
    ) {
        ResumeResponse response = resumeService.generateResume(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(response, "Resume generation completed"));
    }

    @GetMapping("/history")
    @Operation(summary = "Get resume version history", description = "Returns all non-deleted resume versions for authenticated user")
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> getResumeHistory(
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<ResumeResponse> history = resumeService.getResumeHistory(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/{resumeId}")
    @Operation(summary = "Get resume version details", description = "Returns details of a specific resume version")
    public ResponseEntity<ApiResponse<ResumeResponse>> getResumeById(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long resumeId
    ) {
        ResumeResponse resume = resumeService.getResumeById(userPrincipal.getId(), resumeId);
        return ResponseEntity.ok(ApiResponse.success(resume));
    }

    @GetMapping("/{resumeId}/pdf")
    @Operation(summary = "Export resume as PDF", description = "Returns binary PDF download for resume version")
    public ResponseEntity<byte[]> exportPdf(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long resumeId
    ) {
        byte[] pdfBytes = resumeService.exportResumePdf(userPrincipal.getId(), resumeId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resume_" + resumeId + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
    }

    @GetMapping("/{resumeId}/docx")
    @Operation(summary = "Export resume as DOCX", description = "Returns binary DOCX download for resume version")
    public ResponseEntity<byte[]> exportDocx(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long resumeId
    ) {
        byte[] docxBytes = resumeService.exportResumeDocx(userPrincipal.getId(), resumeId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resume_" + resumeId + ".docx")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            .body(docxBytes);
    }

    @DeleteMapping("/{resumeId}")
    @Operation(summary = "Soft delete resume version", description = "Marks a resume version as deleted")
    public ResponseEntity<ApiResponse<Void>> deleteResume(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long resumeId
    ) {
        resumeService.softDeleteResume(userPrincipal.getId(), resumeId);
        return ResponseEntity.ok(ApiResponse.success(null, "Resume soft deleted successfully"));
    }

    @PostMapping("/{resumeId}/analyze")
    @Operation(summary = "Analyze resume ATS match", description = "Triggers ATS scoring and recommendation analysis")
    public ResponseEntity<ApiResponse<AtsAnalysisResponse>> analyzeResume(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long resumeId
    ) {
        ResumeResponse resume = resumeService.getResumeById(userPrincipal.getId(), resumeId);
        AtsAnalysisResponse analysis = atsAnalysisService.getAnalysisByResumeVersionId(resume.getId());
        return ResponseEntity.ok(ApiResponse.success(analysis, "ATS Analysis completed"));
    }
}
