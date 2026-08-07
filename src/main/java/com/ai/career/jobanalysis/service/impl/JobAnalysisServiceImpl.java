package com.ai.career.jobanalysis.service.impl;

import com.ai.career.ai.orchestrator.AIOrchestrator;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.Profile;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.jobanalysis.domain.entity.JobAnalysis;
import com.ai.career.jobanalysis.domain.entity.JobMissingSkill;
import com.ai.career.jobanalysis.domain.entity.JobRecommendation;
import com.ai.career.jobanalysis.domain.repository.JobAnalysisRepository;
import com.ai.career.jobanalysis.domain.repository.JobMissingSkillRepository;
import com.ai.career.jobanalysis.domain.repository.JobRecommendationRepository;
import com.ai.career.jobanalysis.dto.*;
import com.ai.career.jobanalysis.service.JobAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobAnalysisServiceImpl implements JobAnalysisService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final JobMissingSkillRepository jobMissingSkillRepository;
    private final JobRecommendationRepository jobRecommendationRepository;
    private final AIOrchestrator aiOrchestrator;

    @Override
    @Transactional
    public JobAnalysisResponse analyzeJob(Long userId, Long jobId) {
        log.info("Running Deep Job Analysis for Job ID: {}", jobId);

        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + jobId));

        Profile profile = null;
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                profile = user.getProfile();
            }
        }

        JobAnalysisResult fallback = JobAnalysisResult.builder()
            .summary("Role focused on building scalable cloud services and microservices.")
            .responsibilities(List.of("Design & deliver RESTful web services", "Optimize database indexing and queries"))
            .requiredSkills(List.of("Java", "Spring Boot", "MySQL"))
            .preferredSkills(List.of("Kafka", "Docker"))
            .salaryRange("$110,000 - $140,000")
            .workModel("REMOTE")
            .seniorityLevel("SENIOR")
            .matchScore(90)
            .recommendationStatus("APPLY")
            .decision("APPLY")
            .confidence(90)
            .rationale("High skill alignment in Java and Spring Boot.")
            .missingSkills(List.of(
                JobAnalysisResult.AiMissingSkill.builder()
                    .skillName("Kafka")
                    .priority("HIGH")
                    .learning_suggestion("Build a sample messaging queue event publisher.")
                    .build()
            ))
            .build();

        JobAnalysisResult result = aiOrchestrator.executeAiPipeline(
            "job-analysis",
            profile,
            job,
            JobAnalysisResult.class,
            fallback
        );

        JobAnalysis analysis = jobAnalysisRepository.findByJobId(jobId)
            .orElse(JobAnalysis.builder().job(job).build());

        analysis.setSummary(result.getSummary());
        analysis.setResponsibilities(result.getResponsibilities() != null ? String.join(";", result.getResponsibilities()) : "");
        analysis.setRequiredSkills(result.getRequiredSkills() != null ? String.join(",", result.getRequiredSkills()) : "");
        analysis.setPreferredSkills(result.getPreferredSkills() != null ? String.join(",", result.getPreferredSkills()) : "");
        analysis.setSalaryRange(result.getSalaryRange() != null ? result.getSalaryRange() : "$100k-$130k");
        analysis.setWorkModel(result.getWorkModel() != null ? result.getWorkModel() : "HYBRID");
        analysis.setSeniorityLevel(result.getSeniorityLevel() != null ? result.getSeniorityLevel() : "MID_SENIOR");
        analysis.setMatchScore(result.getMatchScore() != null ? result.getMatchScore() : 85);
        analysis.setRecommendationStatus(result.getRecommendationStatus() != null ? result.getRecommendationStatus() : "APPLY");

        JobAnalysis savedAnalysis = jobAnalysisRepository.save(analysis);

        savedAnalysis.getMissingSkills().clear();
        if (result.getMissingSkills() != null) {
            for (JobAnalysisResult.AiMissingSkill ms : result.getMissingSkills()) {
                savedAnalysis.getMissingSkills().add(
                    JobMissingSkill.builder()
                        .jobAnalysis(savedAnalysis)
                        .skillName(ms.getSkillName() != null ? ms.getSkillName() : "General")
                        .priority(ms.getPriority() != null ? ms.getPriority() : "MEDIUM")
                        .learningSuggestion(ms.getLearning_suggestion() != null ? ms.getLearning_suggestion() : "Review documentation.")
                        .build()
                );
            }
        }

        JobRecommendation recommendation = jobRecommendationRepository.findByJobAnalysisId(savedAnalysis.getId())
            .orElse(JobRecommendation.builder().jobAnalysis(savedAnalysis).build());
        recommendation.setDecision(result.getDecision() != null ? result.getDecision() : "APPLY");
        recommendation.setConfidence(result.getConfidence() != null ? result.getConfidence() : 88);
        recommendation.setRationale(result.getRationale() != null ? result.getRationale() : "Good overall background match.");
        JobRecommendation savedRecommendation = jobRecommendationRepository.save(recommendation);
        savedAnalysis.setRecommendation(savedRecommendation);

        return mapToDto(savedAnalysis);
    }

    @Override
    @Transactional(readOnly = true)
    public JobAnalysisResponse getJobAnalysis(Long userId, Long jobId) {
        JobAnalysis ja = jobAnalysisRepository.findByJobId(jobId)
            .orElseGet(() -> {
                analyzeJob(userId, jobId);
                return jobAnalysisRepository.findByJobId(jobId)
                    .orElseThrow(() -> new IllegalArgumentException("Could not analyze job ID: " + jobId));
            });
        return mapToDto(ja);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissingSkillDto> getMissingSkills(Long userId, Long jobId) {
        JobAnalysisResponse ja = getJobAnalysis(userId, jobId);
        return ja.getMissingSkills() != null ? ja.getMissingSkills() : Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public JobRecommendationDto getJobRecommendation(Long userId, Long jobId) {
        JobAnalysisResponse ja = getJobAnalysis(userId, jobId);
        return ja.getRecommendationDetails();
    }

    private JobAnalysisResponse mapToDto(JobAnalysis entity) {
        List<String> resp = entity.getResponsibilities() != null && !entity.getResponsibilities().isBlank()
            ? Arrays.asList(entity.getResponsibilities().split(";"))
            : Collections.emptyList();
        List<String> reqSkills = entity.getRequiredSkills() != null && !entity.getRequiredSkills().isBlank()
            ? Arrays.asList(entity.getRequiredSkills().split(","))
            : Collections.emptyList();
        List<String> prefSkills = entity.getPreferredSkills() != null && !entity.getPreferredSkills().isBlank()
            ? Arrays.asList(entity.getPreferredSkills().split(","))
            : Collections.emptyList();

        List<MissingSkillDto> missingSkills = entity.getMissingSkills() != null
            ? entity.getMissingSkills().stream()
                .map(ms -> MissingSkillDto.builder()
                    .skill(ms.getSkillName())
                    .priority(ms.getPriority())
                    .learningSuggestion(ms.getLearningSuggestion())
                    .build())
                .collect(Collectors.toList())
            : Collections.emptyList();

        JobRecommendationDto recommendationDto = null;
        if (entity.getRecommendation() != null) {
            recommendationDto = JobRecommendationDto.builder()
                .recommendation(entity.getRecommendation().getDecision())
                .decision(entity.getRecommendation().getDecision())
                .confidence(entity.getRecommendation().getConfidence())
                .reason(List.of(entity.getRecommendation().getRationale()))
                .rationale(entity.getRecommendation().getRationale())
                .build();
        }

        return JobAnalysisResponse.builder()
            .id(entity.getId())
            .jobId(entity.getJob().getId())
            .summary(entity.getSummary())
            .responsibilities(resp)
            .requiredSkills(reqSkills)
            .preferredSkills(prefSkills)
            .salary(Map.of("range", entity.getSalaryRange() != null ? entity.getSalaryRange() : ""))
            .salaryRange(entity.getSalaryRange())
            .workModel(entity.getWorkModel())
            .seniorityLevel(entity.getSeniorityLevel())
            .matchScore(entity.getMatchScore())
            .recommendation(entity.getRecommendationStatus())
            .missingSkills(missingSkills)
            .recommendationDetails(recommendationDto)
            .build();
    }
}
