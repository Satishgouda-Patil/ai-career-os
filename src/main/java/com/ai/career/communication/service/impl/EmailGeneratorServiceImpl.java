package com.ai.career.communication.service.impl;

import com.ai.career.ai.orchestrator.AIOrchestrator;
import com.ai.career.communication.domain.entity.EmailDraft;
import com.ai.career.communication.domain.repository.EmailDraftRepository;
import com.ai.career.communication.dto.EmailResponse;
import com.ai.career.communication.dto.EmailResult;
import com.ai.career.communication.dto.GenerateEmailRequest;
import com.ai.career.communication.service.EmailGeneratorService;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.Profile;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.recruiter.domain.entity.Recruiter;
import com.ai.career.recruiter.domain.repository.RecruiterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailGeneratorServiceImpl implements EmailGeneratorService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final RecruiterRepository recruiterRepository;
    private final EmailDraftRepository emailDraftRepository;
    private final AIOrchestrator aiOrchestrator;

    @Override
    @Transactional
    public EmailResponse generateColdEmail(Long userId, GenerateEmailRequest request) {
        log.info("Generating Cold Email for User ID: {}, Job ID: {}", userId, request.getJobId());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Job job = jobRepository.findById(request.getJobId())
            .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + request.getJobId()));

        Recruiter recruiter = null;
        if (request.getRecruiterId() != null) {
            recruiter = recruiterRepository.findById(request.getRecruiterId()).orElse(null);
        }

        Profile profile = user.getProfile();
        String recruiterName = recruiter != null ? recruiter.getName() : "Hiring Manager";
        String fullName = profile != null && profile.getFullName() != null ? profile.getFullName() : user.getEmail();

        EmailResult fallback = EmailResult.builder()
            .subject("Inquiry regarding " + job.getTitle() + " - " + fullName)
            .body("Hi " + recruiterName + ",\n\nI hope this email finds you well. I noticed the " + job.getTitle() + " role at " + job.getCompany() + " and wanted to introduce myself. With my background in backend engineering, I'd love to connect.\n\nBest regards,\n" + fullName)
            .followup("Hi " + recruiterName + ",\n\nFollowing up on my previous message regarding the " + job.getTitle() + " opportunity. Would love to share more context if you're available for a short chat.\n\nBest regards,\n" + fullName)
            .linkedinMessage("Hi " + recruiterName + ", loved seeing the growth at " + job.getCompany() + ". Would love to connect regarding the " + job.getTitle() + " role!")
            .build();

        EmailResult aiResult = aiOrchestrator.executeAiPipeline(
            "cold-email",
            profile,
            job,
            EmailResult.class,
            fallback
        );

        Integer currentMaxVersion = emailDraftRepository.findMaxVersionByUserIdAndJobId(userId, request.getJobId());
        int nextVersion = (currentMaxVersion != null ? currentMaxVersion : 0) + 1;

        EmailDraft emailDraft = EmailDraft.builder()
            .user(user)
            .job(job)
            .recruiter(recruiter)
            .version(nextVersion)
            .subject(aiResult.getSubject() != null ? aiResult.getSubject() : fallback.getSubject())
            .body(aiResult.getBody() != null ? aiResult.getBody() : fallback.getBody())
            .followup(aiResult.getFollowup() != null ? aiResult.getFollowup() : fallback.getFollowup())
            .linkedinMessage(aiResult.getLinkedinMessage() != null ? aiResult.getLinkedinMessage() : fallback.getLinkedinMessage())
            .status("GENERATED")
            .build();

        EmailDraft saved = emailDraftRepository.save(emailDraft);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailResponse getEmailById(Long userId, Long emailId) {
        EmailDraft draft = emailDraftRepository.findById(emailId)
            .orElseThrow(() -> new IllegalArgumentException("Email draft not found with ID: " + emailId));
        return mapToDto(draft);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailResponse getLatestEmailByJobId(Long userId, Long jobId) {
        return emailDraftRepository.findFirstByUserIdAndJobIdOrderByVersionDesc(userId, jobId)
            .map(this::mapToDto)
            .orElseGet(() -> generateColdEmail(userId, GenerateEmailRequest.builder().jobId(jobId).build()));
    }

    @Override
    @Transactional
    public EmailResponse regenerateEmail(Long userId, Long emailId) {
        EmailDraft existing = emailDraftRepository.findById(emailId)
            .orElseThrow(() -> new IllegalArgumentException("Email draft not found with ID: " + emailId));
        GenerateEmailRequest request = GenerateEmailRequest.builder()
            .jobId(existing.getJob().getId())
            .recruiterId(existing.getRecruiter() != null ? existing.getRecruiter().getId() : null)
            .build();
        return generateColdEmail(userId, request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailResponse> getEmailHistory(Long userId) {
        return emailDraftRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    private EmailResponse mapToDto(EmailDraft entity) {
        return EmailResponse.builder()
            .id(entity.getId())
            .emailId(entity.getId())
            .userId(entity.getUser().getId())
            .jobId(entity.getJob().getId())
            .recruiterId(entity.getRecruiter() != null ? entity.getRecruiter().getId() : null)
            .version(entity.getVersion())
            .subject(entity.getSubject())
            .body(entity.getBody())
            .followup(entity.getFollowup())
            .linkedinMessage(entity.getLinkedinMessage())
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
