package com.ai.career.execution.domain.entity;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "browser_execution_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrowserExecutionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "execution_run_id")
    private Long executionRunId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    @Column(name = "event_status", nullable = false, length = 40)
    private String eventStatus;

    @Column(name = "sanitized_reason", columnDefinition = "TEXT")
    private String sanitizedReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
