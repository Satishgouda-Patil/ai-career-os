package com.ai.career.llm.service.impl;

import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.Profile;
import com.ai.career.domain.entity.Skill;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.ProfileRepository;
import com.ai.career.llm.client.OllamaClientService;
import com.ai.career.llm.prompt.PromptTemplates;
import com.ai.career.llm.service.ResumeTailoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaResumeTailorStubImpl implements ResumeTailoringService {

    private final ProfileRepository profileRepository;
    private final JobRepository jobRepository;
    private final OllamaClientService ollamaClientService;

    @Override
    public String generateTailoredSummary(Long userId, Long jobId) {
        Profile profile = profileRepository.findByUserIdWithSkills(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user ID: " + userId));

        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found for ID: " + jobId));

        String userSkillsStr = profile.getSkills().stream()
            .map(Skill::getName)
            .collect(Collectors.joining(", "));

        if (ollamaClientService.isAvailable()) {
            try {
                String prompt = String.format(
                    PromptTemplates.RESUME_TAILORING_PROMPT,
                    profile.getFullName() != null ? profile.getFullName() : "Candidate",
                    profile.getSummary() != null ? profile.getSummary() : "Experienced software developer.",
                    userSkillsStr,
                    job.getTitle(),
                    job.getDescription() != null ? job.getDescription() : ""
                );

                String aiResult = ollamaClientService.generateCompletion(prompt);
                if (aiResult != null && !aiResult.isBlank()) {
                    return aiResult;
                }
            } catch (Exception e) {
                log.warn("Ollama AI generation failed. Using fallback summary stub: {}", e.getMessage());
            }
        }

        // Fallback Stub
        return String.format(
            "Tailored Executive Summary for %s:\n" +
            "Results-oriented professional skilled in %s. " +
            "Proven background aligning technical expertise to excel as %s at %s.",
            profile.getFullName() != null ? profile.getFullName() : "Candidate",
            userSkillsStr.isEmpty() ? "Software Development" : userSkillsStr,
            job.getTitle(),
            job.getCompany()
        );
    }
}
