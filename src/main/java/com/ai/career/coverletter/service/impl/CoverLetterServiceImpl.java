package com.ai.career.coverletter.service.impl;

import com.ai.career.ai.orchestrator.AIOrchestrator;
import com.ai.career.coverletter.domain.entity.CoverLetter;
import com.ai.career.coverletter.domain.repository.CoverLetterRepository;
import com.ai.career.coverletter.dto.CoverLetterResponse;
import com.ai.career.coverletter.dto.CoverLetterResult;
import com.ai.career.coverletter.dto.GenerateCoverLetterRequest;
import com.ai.career.coverletter.service.CoverLetterService;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.Profile;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoverLetterServiceImpl implements CoverLetterService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final CoverLetterRepository coverLetterRepository;
    private final AIOrchestrator aiOrchestrator;

    @Override
    @Transactional
    public CoverLetterResponse generateCoverLetter(Long userId, GenerateCoverLetterRequest request) {
        log.info("Generating Cover Letter for User ID: {}, Job ID: {}", userId, request.getJobId());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Job job = jobRepository.findById(request.getJobId())
            .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + request.getJobId()));

        Profile profile = user.getProfile();
        String tone = request.getTone() != null ? request.getTone() : "Professional";
        String fullName = profile != null && profile.getFullName() != null ? profile.getFullName() : user.getEmail();

        String defaultBody = "Dear Hiring Team at " + job.getCompany() + ",\n\n"
            + "I am writing to express my strong enthusiasm for the " + job.getTitle() + " position. "
            + "With my proven experience in building scalable backend systems, I am confident in my ability to make an immediate positive impact on your engineering team.\n\n"
            + "Thank you for your time and consideration.\n\n"
            + "Sincerely,\n" + fullName;

        CoverLetterResult fallback = CoverLetterResult.builder()
            .tone(tone)
            .content(defaultBody)
            .build();

        CoverLetterResult aiResult = aiOrchestrator.executeAiPipeline(
            "cover-letter",
            profile,
            job,
            CoverLetterResult.class,
            fallback
        );

        Integer currentMaxVersion = coverLetterRepository.findMaxVersionByUserIdAndJobId(userId, request.getJobId());
        int nextVersion = (currentMaxVersion != null ? currentMaxVersion : 0) + 1;

        CoverLetter coverLetter = CoverLetter.builder()
            .user(user)
            .job(job)
            .version(nextVersion)
            .tone(tone)
            .status("GENERATED")
            .content(aiResult.getContent() != null ? aiResult.getContent() : defaultBody)
            .build();

        CoverLetter saved = coverLetterRepository.save(coverLetter);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CoverLetterResponse getLatestCoverLetterByJobId(Long userId, Long jobId) {
        return coverLetterRepository.findFirstByUserIdAndJobIdOrderByVersionDesc(userId, jobId)
            .map(this::mapToDto)
            .orElseGet(() -> generateCoverLetter(userId, GenerateCoverLetterRequest.builder().jobId(jobId).tone("Professional").build()));
    }

    @Override
    @Transactional
    public CoverLetterResponse regenerateCoverLetter(Long userId, Long jobId) {
        CoverLetterResponse existing = getLatestCoverLetterByJobId(userId, jobId);
        String tone = existing != null && existing.getTone() != null ? existing.getTone() : "Professional";
        return generateCoverLetter(userId, GenerateCoverLetterRequest.builder().jobId(jobId).tone(tone).build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoverLetterResponse> getCoverLetterHistory(Long userId) {
        return coverLetterRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    private CoverLetterResponse mapToDto(CoverLetter entity) {
        return CoverLetterResponse.builder()
            .id(entity.getId())
            .coverLetterId(entity.getId())
            .userId(entity.getUser().getId())
            .jobId(entity.getJob().getId())
            .version(entity.getVersion())
            .tone(entity.getTone())
            .status(entity.getStatus())
            .content(entity.getContent())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
