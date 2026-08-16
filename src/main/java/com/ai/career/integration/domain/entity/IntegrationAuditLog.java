package com.ai.career.integration.domain.entity;

import com.ai.career.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "integration_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "request_summary", columnDefinition = "TEXT")
    private String requestSummary;

    @Column(name = "response_summary", columnDefinition = "TEXT")
    private String responseSummary;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
