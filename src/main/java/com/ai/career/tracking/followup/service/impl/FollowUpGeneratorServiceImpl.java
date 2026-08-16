package com.ai.career.tracking.followup.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.tracking.followup.domain.entity.ApplicationFollowUp;
import com.ai.career.tracking.followup.domain.repository.ApplicationFollowUpRepository;
import com.ai.career.tracking.followup.dto.FollowUpDto;
import com.ai.career.tracking.followup.service.FollowUpGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowUpGeneratorServiceImpl implements FollowUpGeneratorService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationFollowUpRepository followUpRepository;

    @Override
    @Transactional
    public FollowUpDto generateFollowUpDraft(Long userId, Long applicationId, Integer sequenceNumber, String customNotes) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!app.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        int seq = (sequenceNumber != null && sequenceNumber > 0) ? sequenceNumber : 1;
        String jobTitle = app.getJob() != null ? app.getJob().getTitle() : "Software Position";
        String company = app.getJob() != null ? app.getJob().getCompany() : "Hiring Team";
        String candidateName = app.getUser() != null && app.getUser().getEmail() != null ? app.getUser().getEmail().split("@")[0] : "Candidate";

        String subject;
        StringBuilder body = new StringBuilder();

        if (seq == 1) {
            subject = "Following Up: Application for " + jobTitle + " at " + company;
            body.append("Dear Hiring Team at ").append(company).append(",\n\n")
                .append("I hope this email finds you well.\n\n")
                .append("I recently submitted my application for the ").append(jobTitle).append(" position and wanted to check in on the status of my application.\n\n")
                .append("I remain very enthusiastic about the opportunity to contribute to ").append(company).append(". ");

            if (customNotes != null && !customNotes.isBlank()) {
                body.append(customNotes.trim()).append(" ");
            }

            body.append("Please let me know if you need any additional information or materials from my end.\n\n")
                .append("Thank you for your time and consideration.\n\n")
                .append("Best regards,\n")
                .append(candidateName);
        } else {
            subject = "Re: Application Status Check — " + jobTitle + " — " + company;
            body.append("Dear ").append(company).append(" Recruitment Team,\n\n")
                .append("I am following up on my previous message regarding the ").append(jobTitle).append(" position.\n\n")
                .append("I understand your team is busy reviewing applications, but I wanted to reaffirm my strong interest in the role. ");

            if (customNotes != null && !customNotes.isBlank()) {
                body.append(customNotes.trim()).append(" ");
            }

            body.append("\n\nI look forward to hearing from you regarding potential next steps.\n\n")
                .append("Sincerely,\n")
                .append(candidateName);
        }

        // Check if follow-up entity exists for this application and sequence number
        List<ApplicationFollowUp> existing = followUpRepository.findByApplicationIdOrderBySequenceNumberAsc(applicationId);
        ApplicationFollowUp followUp = existing.stream()
                .filter(f -> f.getSequenceNumber().equals(seq))
                .findFirst()
                .orElse(null);

        if (followUp == null) {
            LocalDateTime scheduledAt = seq == 1 ? LocalDateTime.now().plusDays(3) : LocalDateTime.now().plusDays(5);
            followUp = ApplicationFollowUp.builder()
                    .application(app)
                    .channel("EMAIL")
                    .sequenceNumber(seq)
                    .scheduledAt(scheduledAt)
                    .status("READY")
                    .followUpSubject(subject)
                    .followUpBody(body.toString())
                    .build();
        } else {
            followUp.setFollowUpSubject(subject);
            followUp.setFollowUpBody(body.toString());
            followUp.setStatus("READY");
        }

        followUp = followUpRepository.save(followUp);
        log.info("Follow-up draft generated for App ID {}, Sequence #{}: Subject: '{}'", applicationId, seq, subject);

        return mapToDto(followUp);
    }

    private FollowUpDto mapToDto(ApplicationFollowUp followUp) {
        return FollowUpDto.builder()
                .id(followUp.getId())
                .applicationId(followUp.getApplication().getId())
                .channel(followUp.getChannel())
                .sequenceNumber(followUp.getSequenceNumber())
                .scheduledAt(followUp.getScheduledAt())
                .status(followUp.getStatus())
                .messageArtifactId(followUp.getMessageArtifactId())
                .followUpSubject(followUp.getFollowUpSubject())
                .followUpBody(followUp.getFollowUpBody())
                .sentAt(followUp.getSentAt())
                .approvedAt(followUp.getApprovedAt())
                .createdAt(followUp.getCreatedAt())
                .build();
    }
}
