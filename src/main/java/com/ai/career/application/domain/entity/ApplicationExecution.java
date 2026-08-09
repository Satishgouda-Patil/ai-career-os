package com.ai.career.application.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "provider_name", nullable = false, length = 100)
    private String providerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationExecutionStatus status;

    @Column(name = "outcome_status", length = 50)
    private String outcomeStatus;

    @Column(name = "external_application_id", length = 255)
    private String externalApplicationId;

    @Column(name = "external_url", length = 1024)
    private String externalUrl;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Builder.Default
    @Column(name = "retryable")
    private boolean retryable = false;

    @Column(name = "execution_logs", columnDefinition = "TEXT")
    private String executionLogs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
