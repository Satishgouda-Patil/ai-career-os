package com.ai.career.resume.service.impl;

import com.ai.career.ai.orchestrator.AIOrchestrator;
import com.ai.career.resume.domain.entity.ResumeAnalysis;
import com.ai.career.resume.domain.entity.ResumeVersion;
import com.ai.career.resume.domain.repository.ResumeAnalysisRepository;
import com.ai.career.resume.dto.AtsAnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtsAnalysisServiceImpl implements com.ai.career.resume.service.AtsAnalysisService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final AIOrchestrator aiOrchestrator;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AtsAnalysisResponse analyzeResume(ResumeVersion resumeVersion) {
        log.info("Running ATS Analysis for ResumeVersion ID: {}", resumeVersion.getId());

        AtsAnalysisResponse fallback = AtsAnalysisResponse.builder()
            .overallScore(85)
            .keywordScore(88)
            .formatScore(82)
            .readabilityScore(85)
            .missingKeywords(List.of("Microservices", "Docker"))
            .recommendations(List.of("Add measurable metrics to achievements", "Ensure clear section headers"))
            .build();

        AtsAnalysisResponse aiResult = aiOrchestrator.executeAiPipeline(
            "ats-analysis",
            resumeVersion.getUser().getProfile(),
            resumeVersion.getJob(),
            AtsAnalysisResponse.class,
            fallback
        );

        String missingKeywordsStr = aiResult.getMissingKeywords() != null
            ? String.join(",", aiResult.getMissingKeywords())
            : "";
        String recommendationsStr = aiResult.getRecommendations() != null
            ? String.join(";", aiResult.getRecommendations())
            : "";

        ResumeAnalysis analysis = resumeAnalysisRepository.findByResumeVersionId(resumeVersion.getId())
            .orElse(ResumeAnalysis.builder().resumeVersion(resumeVersion).build());

        analysis.setOverallScore(aiResult.getOverallScore() != null ? aiResult.getOverallScore() : 80);
        analysis.setKeywordScore(aiResult.getKeywordScore() != null ? aiResult.getKeywordScore() : 80);
        analysis.setFormatScore(aiResult.getFormatScore() != null ? aiResult.getFormatScore() : 80);
        analysis.setReadabilityScore(aiResult.getReadabilityScore() != null ? aiResult.getReadabilityScore() : 80);
        analysis.setMissingKeywords(missingKeywordsStr);
        analysis.setRecommendations(recommendationsStr);

        ResumeAnalysis saved = resumeAnalysisRepository.save(analysis);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AtsAnalysisResponse getAnalysisByResumeVersionId(Long resumeVersionId) {
        return resumeAnalysisRepository.findByResumeVersionId(resumeVersionId)
            .map(this::mapToDto)
            .orElse(null);
    }

    private AtsAnalysisResponse mapToDto(ResumeAnalysis entity) {
        List<String> missing = entity.getMissingKeywords() != null && !entity.getMissingKeywords().isBlank()
            ? Arrays.asList(entity.getMissingKeywords().split(","))
            : Collections.emptyList();
        List<String> recs = entity.getRecommendations() != null && !entity.getRecommendations().isBlank()
            ? Arrays.asList(entity.getRecommendations().split(";"))
            : Collections.emptyList();

        return AtsAnalysisResponse.builder()
            .id(entity.getId())
            .resumeVersionId(entity.getResumeVersion().getId())
            .overallScore(entity.getOverallScore())
            .keywordScore(entity.getKeywordScore())
            .formatScore(entity.getFormatScore())
            .readabilityScore(entity.getReadabilityScore())
            .missingKeywords(missing)
            .recommendations(recs)
            .build();
    }
}
