package com.ai.career.validation.entity;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.validation.model.ApplicationValidationStatus;
import com.ai.career.validation.model.ExecutionReadiness;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_dry_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDryRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "run_id", nullable = false, unique = true, length = 64)
    private String runId;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false, length = 50)
    private ApplicationValidationStatus validationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "readiness_status", nullable = false, length = 50)
    private ExecutionReadiness readinessStatus;

    @Column(name = "dry_run_report_json", columnDefinition = "LONGTEXT", nullable = false)
    private String dryRunReportJson;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
