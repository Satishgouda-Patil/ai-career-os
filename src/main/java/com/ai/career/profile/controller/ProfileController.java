package com.ai.career.profile.controller;

import com.ai.career.llm.service.ResumeTailoringService;
import com.ai.career.profile.dto.ProfileDto;
import com.ai.career.profile.dto.SkillDto;
import com.ai.career.profile.dto.UpdateProfileRequest;
import com.ai.career.profile.service.ProfileService;
import com.ai.career.security.UserPrincipal;
import com.ai.career.storage.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Career Profile", description = "Endpoints for managing user career profile, skills, resume upload, and AI tailoring")
public class ProfileController {

    private final ProfileService profileService;
    private final FileStorageService fileStorageService;
    private final ResumeTailoringService resumeTailoringService;

    @GetMapping
    @Operation(summary = "Get current user career profile")
    public ResponseEntity<ProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        ProfileDto profile = profileService.getProfile(userPrincipal.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    @Operation(summary = "Update current user profile and skill set")
    public ResponseEntity<ProfileDto> updateProfile(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        ProfileDto updatedProfile = profileService.updateProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/skills")
    @Operation(summary = "List all available system skills")
    public ResponseEntity<List<SkillDto>> getAllSkills() {
        return ResponseEntity.ok(profileService.getAllSkills());
    }

    @PostMapping("/resume")
    @Operation(summary = "Upload PDF resume to MinIO object storage")
    public ResponseEntity<Map<String, String>> uploadResume(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @RequestParam("file") MultipartFile file
    ) {
        String fileUrl = fileStorageService.uploadResume(userPrincipal.getId(), file);
        return ResponseEntity.ok(Map.of(
            "message", "Resume uploaded successfully",
            "resumeUrl", fileUrl
        ));
    }

    @PostMapping("/tailor-stub")
    @Operation(summary = "Generate AI-tailored resume executive summary stub for a specific job")
    public ResponseEntity<Map<String, String>> tailorResume(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @RequestParam("jobId") Long jobId
    ) {
        String tailoredSummary = resumeTailoringService.generateTailoredSummary(userPrincipal.getId(), jobId);
        return ResponseEntity.ok(Map.of(
            "jobId", String.valueOf(jobId),
            "tailoredSummary", tailoredSummary
        ));
    }
}
