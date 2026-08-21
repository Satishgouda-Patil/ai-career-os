package com.ai.career.execution.sandbox.domain.entity;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sandbox_execution_runs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SandboxExecutionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "execution_mode", nullable = false, length = 30)
    private String executionMode;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "fields_detected", nullable = false)
    private int fieldsDetected;

    @Column(name = "fields_mapped", nullable = false)
    private int fieldsMapped;

    @Column(name = "fields_verified", nullable = false)
    private int fieldsVerified;

    @Column(name = "fields_require_review", nullable = false)
    private int fieldsRequireReview;

    @Column(name = "submission_simulated", nullable = false)
    private boolean submissionSimulated;

    @Column(name = "submission_verified", nullable = false)
    private boolean submissionVerified;

    @Column(name = "real_submission_attempted", nullable = false)
    private boolean realSubmissionAttempted;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_code", length = 80)
    private String errorCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }
}
