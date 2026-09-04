package com.ai.career.execution.domain.entity;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.domain.entity.User;
import com.ai.career.execution.model.BrowserExecutionMode;
import com.ai.career.execution.model.BrowserExecutionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "browser_execution_runs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrowserExecutionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_mode", nullable = false, length = 30)
    private BrowserExecutionMode executionMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private BrowserExecutionStatus status;

    @Column(name = "preview_id", length = 100)
    private String previewId;

    @Column(name = "form_fingerprint", length = 128)
    private String formFingerprint;

    @Column(name = "submission_attempted", nullable = false)
    private boolean submissionAttempted;

    @Column(name = "submission_verified", nullable = false)
    private boolean submissionVerified;

    @Column(name = "error_code", length = 80)
    private String errorCode;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

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
