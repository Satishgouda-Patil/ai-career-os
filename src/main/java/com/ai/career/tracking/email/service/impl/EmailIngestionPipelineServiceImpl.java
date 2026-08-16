package com.ai.career.tracking.email.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.tracking.email.domain.entity.EmailClassificationResult;
import com.ai.career.tracking.email.domain.entity.EmailMessage;
import com.ai.career.tracking.email.domain.repository.EmailClassificationResultRepository;
import com.ai.career.tracking.email.domain.repository.EmailMessageRepository;
import com.ai.career.tracking.email.dto.*;
import com.ai.career.tracking.email.event.*;
import com.ai.career.tracking.email.provider.EmailProvider;
import com.ai.career.tracking.email.provider.SimulatedEmailProvider;
import com.ai.career.tracking.email.service.*;
import com.ai.career.tracking.service.ApplicationTimelineService;
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
public class EmailIngestionPipelineServiceImpl implements EmailIngestionPipelineService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailClassificationResultRepository classificationResultRepository;
    private final List<EmailProvider> emailProviders;
    private final SimulatedEmailProvider simulatedEmailProvider;
    private final EmailClassifierService classifierService;
    private final ApplicationMatcherService applicationMatcherService;
    private final ApplicationTimelineService timelineService;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public List<EmailMessageDto> ingestUserEmails(Long userId, String providerName) {
        log.info("Starting email ingestion pipeline for User ID {}, Provider: {}", userId, providerName);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        EmailProvider provider = emailProviders.stream()
                .filter(p -> p.getProviderName().equalsIgnoreCase(providerName))
                .findFirst()
                .orElse(simulatedEmailProvider);

        List<RawEmailMessageDto> rawMessages = provider.fetchUnprocessedMessages(userId);
        if (rawMessages.isEmpty()) {
            log.info("No new email messages fetched from provider {}", providerName);
            return Collections.emptyList();
        }

        List<EmailMessageDto> processedDtos = new ArrayList<>();
        for (RawEmailMessageDto raw : rawMessages) {
            EmailMessageDto dto = processSingleEmail(user, raw, null);
            processedDtos.add(dto);
        }

        return processedDtos;
    }

    @Override
    @Transactional
    public EmailMessageDto ingestSimulatedEmail(Long userId, SimulateEmailRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        RawEmailMessageDto raw = RawEmailMessageDto.builder()
                .provider("SIMULATED")
                .externalMessageId("sim-msg-" + UUID.randomUUID().toString().substring(0, 8))
                .externalThreadId(request.getExternalThreadId() != null ? request.getExternalThreadId() : "sim-thread-1")
                .sender(request.getSender())
                .subject(request.getSubject())
                .bodySnippet(request.getBodySnippet())
                .receivedAt(LocalDateTime.now())
                .build();

        return processSingleEmail(user, raw, request.getApplicationId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailMessageDto> getUserEmails(Long userId) {
        return emailMessageRepository.findByUserIdOrderByReceivedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmailMessageDto getEmailDetails(Long userId, Long emailId) {
        EmailMessage message = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new IllegalArgumentException("Email message not found with ID: " + emailId));

        if (!message.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to email ID: " + emailId);
        }

        return mapToDto(message);
    }

    @Override
    @Transactional
    public EmailMessageDto manuallyMatchApplication(Long userId, Long emailId, Long applicationId) {
        EmailMessage message = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new IllegalArgumentException("Email message not found with ID: " + emailId));

        if (!message.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to email ID: " + emailId);
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        message.setApplication(application);
        message = emailMessageRepository.save(message);

        // Record timeline activity
        timelineService.recordActivity(
                userId,
                applicationId,
                message.getClassification() != null ? message.getClassification() : "EMAIL_MANUALLY_LINKED",
                "USER",
                "Email manually associated: " + message.getSubject(),
                Map.of("emailId", message.getId(), "sender", message.getSender()),
                1.0
        );

        return mapToDto(message);
    }

    private EmailMessageDto processSingleEmail(User user, RawEmailMessageDto raw, Long explicitAppId) {
        // Idempotency check
        Optional<EmailMessage> existing = emailMessageRepository.findByProviderAndExternalMessageId(raw.getProvider(), raw.getExternalMessageId());
        if (existing.isPresent()) {
            return mapToDto(existing.get());
        }

        String senderDomain = null;
        if (raw.getSender() != null && raw.getSender().contains("@")) {
            senderDomain = raw.getSender().substring(raw.getSender().indexOf("@") + 1);
        }

        // 1. Save base EmailMessage
        EmailMessage message = EmailMessage.builder()
                .user(user)
                .provider(raw.getProvider() != null ? raw.getProvider() : "SIMULATED")
                .externalMessageId(raw.getExternalMessageId())
                .externalThreadId(raw.getExternalThreadId())
                .sender(raw.getSender())
                .senderDomain(senderDomain)
                .subject(raw.getSubject())
                .bodySnippet(raw.getBodySnippet())
                .receivedAt(raw.getReceivedAt() != null ? raw.getReceivedAt() : LocalDateTime.now())
                .build();

        message = emailMessageRepository.save(message);

        // 2. Classify
        EmailClassificationDto classification = classifierService.classify(raw.getSender(), raw.getSubject(), raw.getBodySnippet());
        message.setClassification(classification.getClassification());
        message.setClassificationConfidence(classification.getConfidence());

        String extractedJson = null;
        if (classification.getExtractedData() != null) {
            try {
                extractedJson = objectMapper.writeValueAsString(classification.getExtractedData());
            } catch (Exception ignored) {}
        }

        EmailClassificationResult classificationResult = EmailClassificationResult.builder()
                .emailMessage(message)
                .classification(classification.getClassification())
                .confidence(classification.getConfidence())
                .extractedDataJson(extractedJson)
                .model(classification.getModel())
                .build();

        classificationResultRepository.save(classificationResult);

        // 3. Application Matching
        Application matchedApp = null;
        if (explicitAppId != null) {
            matchedApp = applicationRepository.findById(explicitAppId).orElse(null);
        }

        if (matchedApp == null) {
            Optional<ApplicationMatcherService.ApplicationMatchResult> matchResult = applicationMatcherService.matchApplication(
                    user.getId(), raw.getSender(), raw.getSubject(), raw.getBodySnippet(), raw.getExternalThreadId()
            );
            if (matchResult.isPresent()) {
                matchedApp = matchResult.get().application();
            }
        }

        if (matchedApp != null) {
            message.setApplication(matchedApp);

            // Record Timeline Activity & Update Status
            String category = classification.getClassification();
            recordActivityAndSetState(user.getId(), matchedApp, category, message, classification.getConfidence());
        }

        message.setProcessedAt(LocalDateTime.now());
        message = emailMessageRepository.save(message);

        // 4. Publish Domain Events
        publishDomainEvents(message, classification);

        return mapToDto(message);
    }

    private void recordActivityAndSetState(Long userId, Application app, String category, EmailMessage message, Double confidence) {
        Map<String, Object> meta = Map.of(
                "emailId", message.getId(),
                "subject", message.getSubject() != null ? message.getSubject() : "",
                "sender", message.getSender() != null ? message.getSender() : ""
        );

        if ("APPLICATION_CONFIRMATION".equalsIgnoreCase(category)) {
            timelineService.recordActivity(userId, app.getId(), "CONFIRMATION_RECEIVED", "EMAIL", "Application confirmation email received", meta, confidence);
        } else if ("RECRUITER_RESPONSE".equalsIgnoreCase(category)) {
            timelineService.recordActivity(userId, app.getId(), "RECRUITER_RESPONDED", "EMAIL", "Recruiter responded: " + message.getSubject(), meta, confidence);
        } else if ("INTERVIEW_INVITATION".equalsIgnoreCase(category)) {
            timelineService.recordActivity(userId, app.getId(), "INTERVIEW_SCHEDULED", "EMAIL", "Interview invitation received: " + message.getSubject(), meta, confidence);
            if (app.getStatus() != ApplicationState.REJECTED && app.getStatus() != ApplicationState.WITHDRAWN) {
                app.setStatus(ApplicationState.INTERVIEW);
                applicationRepository.save(app);
            }
        } else if ("REJECTION".equalsIgnoreCase(category)) {
            timelineService.recordActivity(userId, app.getId(), "REJECTION_RECEIVED", "EMAIL", "Rejection notice received: " + message.getSubject(), meta, confidence);
            app.setStatus(ApplicationState.REJECTED);
            applicationRepository.save(app);
        } else if ("OFFER".equalsIgnoreCase(category)) {
            timelineService.recordActivity(userId, app.getId(), "OFFER_RECEIVED", "EMAIL", "Offer letter received: " + message.getSubject(), meta, confidence);
            app.setStatus(ApplicationState.OFFER);
            applicationRepository.save(app);
        }
    }

    private void publishDomainEvents(EmailMessage message, EmailClassificationDto classification) {
        String corrId = UUID.randomUUID().toString();
        Long appId = message.getApplication() != null ? message.getApplication().getId() : 0L;

        eventPublisher.publishEvent(EmailReceivedEvent.builder()
                .messageId(message.getId())
                .applicationId(appId)
                .userId(message.getUser().getId())
                .provider(message.getProvider())
                .externalMessageId(message.getExternalMessageId())
                .externalThreadId(message.getExternalThreadId())
                .sender(message.getSender())
                .subject(message.getSubject())
                .correlationId(corrId)
                .timestamp(LocalDateTime.now())
                .build());

        eventPublisher.publishEvent(JobEmailClassifiedEvent.builder()
                .messageId(message.getId())
                .applicationId(appId)
                .userId(message.getUser().getId())
                .classification(classification.getClassification())
                .confidence(classification.getConfidence())
                .correlationId(corrId)
                .timestamp(LocalDateTime.now())
                .build());

        String category = classification.getClassification();
        if ("RECRUITER_RESPONSE".equalsIgnoreCase(category)) {
            eventPublisher.publishEvent(RecruiterResponseDetectedEvent.builder()
                    .messageId(message.getId())
                    .applicationId(appId)
                    .userId(message.getUser().getId())
                    .confidence(classification.getConfidence())
                    .correlationId(corrId)
                    .timestamp(LocalDateTime.now())
                    .build());
        } else if ("REJECTION".equalsIgnoreCase(category)) {
            eventPublisher.publishEvent(RejectionDetectedEvent.builder()
                    .messageId(message.getId())
                    .applicationId(appId)
                    .userId(message.getUser().getId())
                    .confidence(classification.getConfidence())
                    .correlationId(corrId)
                    .timestamp(LocalDateTime.now())
                    .build());
        } else if ("INTERVIEW_INVITATION".equalsIgnoreCase(category)) {
            String meetingUrl = classification.getExtractedData() != null ? (String) classification.getExtractedData().get("meetingUrl") : null;
            eventPublisher.publishEvent(InterviewDetectedEvent.builder()
                    .messageId(message.getId())
                    .applicationId(appId)
                    .userId(message.getUser().getId())
                    .confidence(classification.getConfidence())
                    .meetingUrl(meetingUrl)
                    .correlationId(corrId)
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    private EmailMessageDto mapToDto(EmailMessage message) {
        return EmailMessageDto.builder()
                .id(message.getId())
                .userId(message.getUser().getId())
                .provider(message.getProvider())
                .externalMessageId(message.getExternalMessageId())
                .externalThreadId(message.getExternalThreadId())
                .sender(message.getSender())
                .senderDomain(message.getSenderDomain())
                .subject(message.getSubject())
                .bodySnippet(message.getBodySnippet())
                .receivedAt(message.getReceivedAt())
                .classification(message.getClassification())
                .classificationConfidence(message.getClassificationConfidence())
                .applicationId(message.getApplication() != null ? message.getApplication().getId() : null)
                .processedAt(message.getProcessedAt())
                .build();
    }
}
