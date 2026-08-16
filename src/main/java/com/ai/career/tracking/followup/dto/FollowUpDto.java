package com.ai.career.tracking.followup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowUpDto {
    private Long id;
    private Long applicationId;
    private String channel;
    private Integer sequenceNumber;
    private LocalDateTime scheduledAt;
    private String status;
    private Long messageArtifactId;
    private String followUpSubject;
    private String followUpBody;
    private LocalDateTime sentAt;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
