package com.ai.career.match.service.impl;

import com.ai.career.config.RabbitMQConfig;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.JobMatch;
import com.ai.career.domain.entity.Profile;
import com.ai.career.domain.entity.Skill;
import com.ai.career.domain.repository.JobMatchRepository;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.ProfileRepository;
import com.ai.career.match.event.MatchFoundEvent;
import com.ai.career.match.service.MatchScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchScoringServiceImpl implements MatchScoringService {

    private final ProfileRepository profileRepository;
    private final JobRepository jobRepository;
    private final JobMatchRepository jobMatchRepository;
    private final RabbitTemplate rabbitTemplate;
    private final com.ai.career.llm.service.LlmMatchEvaluator llmMatchEvaluator;

    @Value("${app.rabbitmq.exchange:job.exchange}")
    private String exchangeName;

    @Value("${app.match.threshold:75}")
    private int matchThreshold;

    @Override
    @Transactional
    public void processJobMatching(List<Long> jobIds) {
        List<Profile> profiles = profileRepository.findAll();
        List<Job> jobs = jobRepository.findAllById(jobIds);

        for (Profile profile : profiles) {
            for (Job job : jobs) {
                evaluateAndSaveMatch(profile, job);
            }
        }
    }

    @Override
    @Transactional
    public void processProfileMatching(Long userId) {
        Profile profile = profileRepository.findByUserIdWithSkills(userId).orElse(null);
        if (profile == null) return;

        List<Job> jobs = jobRepository.findAll();
        for (Job job : jobs) {
            evaluateAndSaveMatch(profile, job);
        }
    }

    @Override
    public int calculateMatchScore(SetOfSkills profileSkills, String jobTitle, String jobDescription) {
        if (profileSkills == null || profileSkills.getSkillNames().isEmpty()) {
            return 0;
        }

        // 1. Try local LLM evaluation
        Integer llmScore = llmMatchEvaluator.evaluateMatchWithLlm(profileSkills.getSkillNames(), jobTitle, jobDescription);
        if (llmScore != null) {
            return llmScore;
        }

        // 2. Fallback to keyword matching
        String textToSearch = ((jobTitle != null ? jobTitle : "") + " " + (jobDescription != null ? jobDescription : "")).toLowerCase();

        long matchedCount = profileSkills.getSkillNames().stream()
            .map(String::toLowerCase)
            .filter(textToSearch::contains)
            .count();

        int totalSkills = profileSkills.getSkillNames().size();
        return (int) Math.min(100, Math.round(((double) matchedCount / totalSkills) * 100));
    }

    private void evaluateAndSaveMatch(Profile profile, Job job) {
        Set<String> userSkillNames = profile.getSkills().stream()
            .map(Skill::getName)
            .collect(Collectors.toSet());

        SetOfSkills skills = () -> List.copyOf(userSkillNames);
        int score = calculateMatchScore(skills, job.getTitle(), job.getDescription());

        JobMatch match = jobMatchRepository.findByProfileUserIdAndJobId(profile.getUserId(), job.getId())
            .orElse(JobMatch.builder().profile(profile).job(job).build());

        match.setScore(score);
        JobMatch savedMatch = jobMatchRepository.save(match);

        if (score >= matchThreshold) {
            MatchFoundEvent event = MatchFoundEvent.builder()
                .matchId(savedMatch.getId())
                .userId(profile.getUserId())
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .company(job.getCompany())
                .score(score)
                .build();

            try {
                rabbitTemplate.convertAndSend(exchangeName, RabbitMQConfig.ROUTING_KEY_MATCH_FOUND, event);
                log.info("High match score {} detected for User {} and Job {}. Published MatchFoundEvent.", score, profile.getUserId(), job.getId());
            } catch (Exception ex) {
                log.warn("RabbitMQ offline. Could not publish MatchFoundEvent: {}", ex.getMessage());
            }
        }
    }
}
