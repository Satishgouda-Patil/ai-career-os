package com.ai.career.tracking.interview.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Profile;
import com.ai.career.domain.repository.ProfileRepository;
import com.ai.career.tracking.interview.domain.entity.Interview;
import com.ai.career.tracking.interview.domain.entity.InterviewPreparation;
import com.ai.career.tracking.interview.domain.repository.InterviewPreparationRepository;
import com.ai.career.tracking.interview.domain.repository.InterviewRepository;
import com.ai.career.tracking.interview.dto.InterviewDto;
import com.ai.career.tracking.interview.dto.InterviewPrepDto;
import com.ai.career.tracking.interview.dto.ScheduleInterviewRequest;
import com.ai.career.tracking.interview.event.InterviewPrepGeneratedEvent;
import com.ai.career.tracking.interview.event.InterviewScheduledEvent;
import com.ai.career.tracking.interview.service.InterviewPrepService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewPrepServiceImpl implements InterviewPrepService {

    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewPreparationRepository prepRepository;
    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public InterviewDto createInterview(Long userId, Long applicationId, ScheduleInterviewRequest request) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!app.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        Interview interview = Interview.builder()
                .application(app)
                .interviewType(request != null && request.getInterviewType() != null ? request.getInterviewType() : "GENERAL")
                .scheduledAt(request != null && request.getScheduledAt() != null ? request.getScheduledAt() : LocalDateTime.now().plusDays(2))
                .timezone(request != null ? request.getTimezone() : "UTC")
                .meetingUrl(request != null ? request.getMeetingUrl() : null)
                .interviewerName(request != null ? request.getInterviewerName() : null)
                .interviewerTitle(request != null ? request.getInterviewerTitle() : null)
                .status("SCHEDULED")
                .notes(request != null ? request.getNotes() : null)
                .build();

        interview = interviewRepository.save(interview);

        if (app.getStatus() != ApplicationState.REJECTED && app.getStatus() != ApplicationState.WITHDRAWN) {
            app.setStatus(ApplicationState.INTERVIEW);
            applicationRepository.save(app);
        }

        log.info("Interview created for App ID {}, Type: {}", applicationId, interview.getInterviewType());

        eventPublisher.publishEvent(InterviewScheduledEvent.builder()
                .interviewId(interview.getId())
                .applicationId(applicationId)
                .userId(userId)
                .interviewType(interview.getInterviewType())
                .meetingUrl(interview.getMeetingUrl())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build());

        // Auto-generate prep kit
        generatePrepWorkspace(userId, interview.getId());

        return mapToDto(interview);
    }

    @Override
    @Transactional
    public InterviewPrepDto generatePrepWorkspace(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found with ID: " + interviewId));

        Application app = interview.getApplication();
        if (!app.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to interview ID: " + interviewId);
        }

        String company = app.getJob() != null ? app.getJob().getCompany() : "Target Company";
        String title = app.getJob() != null ? app.getJob().getTitle() : "Software Engineer";
        String jobDesc = app.getJob() != null && app.getJob().getDescription() != null ? app.getJob().getDescription() : "";

        Profile profile = profileRepository.findById(userId).orElse(null);

        // 1. Company Overview
        Map<String, Object> companyOverview = Map.of(
                "companyName", company,
                "summary", company + " operates in software & technology domain. Review mission statement, recent product launches, and engineering tech stack before interview.",
                "focusAreas", List.of("Scalable Distributed Architecture", "Product Quality & Speed", "Engineering Culture")
        );

        // 2. Role Focus
        Map<String, Object> roleFocus = Map.of(
                "jobTitle", title,
                "responsibilities", List.of(
                        "Design and maintain mission-critical software systems.",
                        "Collaborate with product and cross-functional teams to deliver high quality features.",
                        "Perform code reviews, system design, and continuous optimization."
                ),
                "requiredSkills", List.of("Java / Spring Boot", "System Design & REST APIs", "Database Optimization & Microservices")
        );

        // 3. Candidate Talking Points
        String profileHeadline = profile != null && profile.getFullName() != null ? profile.getFullName() + " — Software Professional" : "Experienced Software Specialist";
        Map<String, Object> candidateTalkingPoints = Map.of(
                "headline", profileHeadline,
                "strongestExperience", "Highlight backend architecture, system design wins, and production system reliability.",
                "resumeTalkingPoints", List.of(
                        "Walk through a key project where you designed distributed services under high load.",
                        "Discuss your experience with microservices, databases, and message queues.",
                        "Share an instance where you resolved a critical production bottleneck."
                ),
                "potentialWeakAreasToAddress", List.of("Be ready to explain transition between legacy systems and modern cloud-native architectures.")
        );

        // 4. Sample Questions
        List<Map<String, String>> sampleQuestions = List.of(
                Map.of("category", "BEHAVIORAL", "question", "Tell me about a complex technical conflict you resolved in a project."),
                Map.of("category", "TECHNICAL", "question", "How do you design a resilient microservice system that handles transient database failures?"),
                Map.of("category", "ROLE_SPECIFIC", "question", "What architecture choices would you make when building services for " + company + "?"),
                Map.of("category", "COMPANY_SPECIFIC", "question", "Why are you specifically interested in joining " + company + " at this stage?")
        );

        // 5. Questions to Ask Interviewer
        List<String> questionsToAsk = List.of(
                "What does success look like in the first 90 days for this " + title + " role?",
                "How does engineering collaboration work between product management, DevOps, and developers at " + company + "?",
                "What are the biggest technical growth challenges the team plans to solve in the coming year?"
        );

        String companyOverviewJson = toJson(companyOverview);
        String roleFocusJson = toJson(roleFocus);
        String candidateTalkingPointsJson = toJson(candidateTalkingPoints);
        String sampleQuestionsJson = toJson(sampleQuestions);
        String questionsToAskJson = toJson(questionsToAsk);

        InterviewPreparation prep = prepRepository.findTopByInterviewIdOrderByGeneratedAtDesc(interviewId).orElse(null);
        if (prep == null) {
            prep = InterviewPreparation.builder()
                    .interview(interview)
                    .companyOverviewJson(companyOverviewJson)
                    .roleFocusJson(roleFocusJson)
                    .candidateTalkingPointsJson(candidateTalkingPointsJson)
                    .sampleQuestionsJson(sampleQuestionsJson)
                    .questionsToAskJson(questionsToAskJson)
                    .build();
        } else {
            prep.setCompanyOverviewJson(companyOverviewJson);
            prep.setRoleFocusJson(roleFocusJson);
            prep.setCandidateTalkingPointsJson(candidateTalkingPointsJson);
            prep.setSampleQuestionsJson(sampleQuestionsJson);
            prep.setQuestionsToAskJson(questionsToAskJson);
        }

        prep = prepRepository.save(prep);
        log.info("Interview preparation workspace generated for Interview ID {}", interviewId);

        eventPublisher.publishEvent(InterviewPrepGeneratedEvent.builder()
                .interviewId(interviewId)
                .prepId(prep.getId())
                .applicationId(app.getId())
                .userId(userId)
                .correlationId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build());

        return mapToPrepDto(prep);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewPrepDto getInterviewPrep(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found with ID: " + interviewId));

        if (!interview.getApplication().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to interview ID: " + interviewId);
        }

        InterviewPreparation prep = prepRepository.findTopByInterviewIdOrderByGeneratedAtDesc(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview preparation workspace not found for Interview ID: " + interviewId));

        return mapToPrepDto(prep);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewDto getInterviewDetails(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found with ID: " + interviewId));

        if (!interview.getApplication().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to interview ID: " + interviewId);
        }

        return mapToDto(interview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewDto> getApplicationInterviews(Long userId, Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!app.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        return interviewRepository.findByApplicationIdOrderByScheduledAtDesc(applicationId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewDto> getUserInterviews(Long userId) {
        return interviewRepository.findByApplicationUserIdOrderByScheduledAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private InterviewDto mapToDto(Interview i) {
        String company = i.getApplication() != null && i.getApplication().getJob() != null ? i.getApplication().getJob().getCompany() : "";
        String title = i.getApplication() != null && i.getApplication().getJob() != null ? i.getApplication().getJob().getTitle() : "";
        return InterviewDto.builder()
                .id(i.getId())
                .applicationId(i.getApplication().getId())
                .companyName(company)
                .jobTitle(title)
                .interviewType(i.getInterviewType())
                .scheduledAt(i.getScheduledAt())
                .timezone(i.getTimezone())
                .meetingUrl(i.getMeetingUrl())
                .interviewerName(i.getInterviewerName())
                .interviewerTitle(i.getInterviewerTitle())
                .status(i.getStatus())
                .notes(i.getNotes())
                .createdAt(i.getCreatedAt())
                .build();
    }

    private InterviewPrepDto mapToPrepDto(InterviewPreparation prep) {
        Map<String, Object> companyOverview = fromJson(prep.getCompanyOverviewJson(), new TypeReference<>() {});
        Map<String, Object> roleFocus = fromJson(prep.getRoleFocusJson(), new TypeReference<>() {});
        Map<String, Object> candidateTalkingPoints = fromJson(prep.getCandidateTalkingPointsJson(), new TypeReference<>() {});
        List<Map<String, String>> sampleQuestions = fromJson(prep.getSampleQuestionsJson(), new TypeReference<>() {});
        List<String> questionsToAsk = fromJson(prep.getQuestionsToAskJson(), new TypeReference<>() {});

        return InterviewPrepDto.builder()
                .id(prep.getId())
                .interviewId(prep.getInterview().getId())
                .companyOverview(companyOverview)
                .roleFocus(roleFocus)
                .candidateTalkingPoints(candidateTalkingPoints)
                .sampleQuestions(sampleQuestions)
                .questionsToAsk(questionsToAsk)
                .generatedAt(prep.getGeneratedAt())
                .build();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            return null;
        }
    }
}
