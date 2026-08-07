package com.ai.career.resume.service.impl;

import com.ai.career.ai.orchestrator.AIOrchestrator;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.Profile;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.resume.domain.entity.ResumeVersion;
import com.ai.career.resume.domain.repository.ResumeVersionRepository;
import com.ai.career.resume.dto.AtsAnalysisResponse;
import com.ai.career.resume.dto.GenerateResumeRequest;
import com.ai.career.resume.dto.GeneratedResumeContent;
import com.ai.career.resume.dto.ResumeResponse;
import com.ai.career.resume.export.ResumeExportService;
import com.ai.career.resume.service.AtsAnalysisService;
import com.ai.career.resume.service.ResumeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ResumeVersionRepository resumeVersionRepository;
    private final AIOrchestrator aiOrchestrator;
    private final AtsAnalysisService atsAnalysisService;
    private final ResumeExportService resumeExportService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ResumeResponse generateResume(Long userId, GenerateResumeRequest request) {
        log.info("Generating ATS Resume for User ID: {}, Job ID: {}", userId, request.getJobId());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Job job = null;
        if (request.getJobId() != null) {
            job = jobRepository.findById(request.getJobId()).orElse(null);
        }

        Profile profile = user.getProfile();
        String fullName = profile != null && profile.getFullName() != null ? profile.getFullName() : user.getEmail();

        GeneratedResumeContent fallback = GeneratedResumeContent.builder()
            .fullName(fullName)
            .headline("Targeted Professional")
            .summary(profile != null && profile.getSummary() != null ? profile.getSummary() : "Experienced software professional.")
            .skills(List.of("Java", "Spring Boot", "MySQL", "REST API"))
            .experience(List.of(
                GeneratedResumeContent.ExperienceItem.builder()
                    .title("Software Engineer")
                    .company(job != null ? job.getCompany() : "Tech Solutions")
                    .period("2022 - Present")
                    .highlights(List.of("Developed scalable microservices", "Optimized database queries by 35%"))
                    .build()
            ))
            .atsScore(90)
            .build();

        GeneratedResumeContent generated = aiOrchestrator.executeAiPipeline(
            "resume-generation",
            profile,
            job,
            GeneratedResumeContent.class,
            fallback
        );

        String jsonContent;
        try {
            jsonContent = objectMapper.writeValueAsString(generated);
        } catch (Exception e) {
            jsonContent = "{}";
        }

        Integer currentMaxVersion = resumeVersionRepository.findMaxVersionByUserId(userId);
        int nextVersion = (currentMaxVersion != null ? currentMaxVersion : 0) + 1;

        ResumeVersion resumeVersion = ResumeVersion.builder()
            .user(user)
            .job(job)
            .templateName(request.getTemplate() != null ? request.getTemplate() : "MODERN")
            .version(nextVersion)
            .status("GENERATED")
            .contentJson(jsonContent)
            .deleted(false)
            .build();

        ResumeVersion savedVersion = resumeVersionRepository.save(resumeVersion);

        String pdfUrl = resumeExportService.generateAndStorePdf(savedVersion);
        String docxUrl = resumeExportService.generateAndStoreDocx(savedVersion);
        savedVersion.setPdfUrl(pdfUrl);
        savedVersion.setDocxUrl(docxUrl);
        savedVersion = resumeVersionRepository.save(savedVersion);

        AtsAnalysisResponse analysis = atsAnalysisService.analyzeResume(savedVersion);

        return mapToDto(savedVersion, analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponse> getResumeHistory(Long userId) {
        return resumeVersionRepository.findByUserIdAndDeletedFalseOrderByVersionDesc(userId)
            .stream()
            .map(rv -> mapToDto(rv, atsAnalysisService.getAnalysisByResumeVersionId(rv.getId())))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getResumeById(Long userId, Long resumeId) {
        ResumeVersion rv = resumeVersionRepository.findByIdAndUserIdAndDeletedFalse(resumeId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found with ID: " + resumeId));
        return mapToDto(rv, atsAnalysisService.getAnalysisByResumeVersionId(rv.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportResumePdf(Long userId, Long resumeId) {
        ResumeVersion rv = resumeVersionRepository.findByIdAndUserIdAndDeletedFalse(resumeId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found with ID: " + resumeId));
        String content = "PDF EXPORT FOR " + rv.getUser().getEmail() + "\nVersion: " + rv.getVersion() + "\n" + rv.getContentJson();
        return content.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportResumeDocx(Long userId, Long resumeId) {
        ResumeVersion rv = resumeVersionRepository.findByIdAndUserIdAndDeletedFalse(resumeId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found with ID: " + resumeId));
        String content = "DOCX EXPORT FOR " + rv.getUser().getEmail() + "\nVersion: " + rv.getVersion() + "\n" + rv.getContentJson();
        return content.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional
    public void softDeleteResume(Long userId, Long resumeId) {
        ResumeVersion rv = resumeVersionRepository.findByIdAndUserIdAndDeletedFalse(resumeId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Resume not found with ID: " + resumeId));
        rv.setDeleted(true);
        resumeVersionRepository.save(rv);
    }

    private ResumeResponse mapToDto(ResumeVersion entity, AtsAnalysisResponse analysis) {
        return ResumeResponse.builder()
            .id(entity.getId())
            .userId(entity.getUser().getId())
            .jobId(entity.getJob() != null ? entity.getJob().getId() : null)
            .templateName(entity.getTemplateName())
            .version(entity.getVersion())
            .status(entity.getStatus())
            .contentJson(entity.getContentJson())
            .pdfUrl(entity.getPdfUrl())
            .docxUrl(entity.getDocxUrl())
            .createdAt(entity.getCreatedAt())
            .analysis(analysis)
            .build();
    }
}
