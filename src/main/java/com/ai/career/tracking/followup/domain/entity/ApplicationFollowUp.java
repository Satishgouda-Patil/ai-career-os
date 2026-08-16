package com.ai.career.tracking.followup.domain.entity;

import com.ai.career.application.domain.entity.Application;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_follow_ups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationFollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "channel", nullable = false, length = 50)
    @Builder.Default
    private String channel = "EMAIL";

    @Column(name = "sequence_number", nullable = false)
    @Builder.Default
    private Integer sequenceNumber = 1;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "SCHEDULED";

    @Column(name = "message_artifact_id")
    private Long messageArtifactId;

    @Column(name = "follow_up_subject", length = 512)
    private String followUpSubject;

    @Column(name = "follow_up_body", columnDefinition = "TEXT")
    private String followUpBody;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
