package com.ai.career.execution.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "controlled_application_executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControlledApplicationExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tenant_id", length = 100)
    @Builder.Default
    private String tenantId = "default";

    @Column(name = "execution_run_id", length = 100)
    private String executionRunId;

    @Column(name = "preview_id", length = 100)
    private String previewId;

    @Column(name = "confirmation_id", length = 100)
    private String confirmationId;

    @Column(name = "form_fingerprint", length = 255)
    private String formFingerprint;

    @Column(name = "workflow_status", nullable = false, length = 50)
    private String workflowStatus;

    @Column(name = "execution_mode", nullable = false, length = 50)
    private String executionMode;

    @Column(name = "current_step", nullable = false)
    @Builder.Default
    private int currentStep = 1;

    @Column(name = "failure_code", length = 100)
    private String failureCode;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "verification_status", length = 50)
    private String verificationStatus;

    @Column(name = "submission_attempted", nullable = false)
    @Builder.Default
    private boolean submissionAttempted = false;

    @Column(name = "email_sent", nullable = false)
    @Builder.Default
    private boolean emailSent = false;

    @Column(name = "file_uploaded", nullable = false)
    @Builder.Default
    private boolean fileUploaded = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
