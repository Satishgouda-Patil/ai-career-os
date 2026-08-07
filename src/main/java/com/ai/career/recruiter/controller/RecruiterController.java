package com.ai.career.recruiter.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.recruiter.dto.DiscoverRecruiterRequest;
import com.ai.career.recruiter.dto.RecruiterResponse;
import com.ai.career.recruiter.service.RecruiterDiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recruiters")
@RequiredArgsConstructor
@Tag(name = "Recruiter Intelligence", description = "Recruiter & Talent Acquisition Discovery APIs")
public class RecruiterController {

    private final RecruiterDiscoveryService recruiterDiscoveryService;

    @PostMapping("/discover")
    @Operation(summary = "Discover recruiters", description = "Discovers recruiters for target company using configured provider SPI")
    public ResponseEntity<ApiResponse<Map<String, Object>>> discoverRecruiters(
        @Valid @RequestBody DiscoverRecruiterRequest request
    ) {
        recruiterDiscoveryService.discoverRecruiters(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(Map.of("status", "PROCESSING", "jobId", "DISCOVERY-001"), "Recruiter discovery task initiated"));
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get company recruiters", description = "Returns discovered recruiters for target company")
    public ResponseEntity<ApiResponse<List<RecruiterResponse>>> getCompanyRecruiters(
        @PathVariable Long companyId
    ) {
        List<RecruiterResponse> recruiters = recruiterDiscoveryService.getRecruitersByCompanyId(companyId);
        return ResponseEntity.ok(ApiResponse.success(recruiters));
    }

    @GetMapping("/{recruiterId}")
    @Operation(summary = "Get recruiter details", description = "Returns detailed profile of a recruiter")
    public ResponseEntity<ApiResponse<RecruiterResponse>> getRecruiterById(
        @PathVariable Long recruiterId
    ) {
        RecruiterResponse recruiter = recruiterDiscoveryService.getRecruiterById(recruiterId);
        return ResponseEntity.ok(ApiResponse.success(recruiter));
    }
}
