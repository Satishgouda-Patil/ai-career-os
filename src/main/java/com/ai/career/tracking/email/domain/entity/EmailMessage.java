package com.ai.career.tracking.email.domain.entity;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "email_messages",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_provider_external_msg", columnNames = {"provider", "external_message_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider", nullable = false, length = 50)
    @Builder.Default
    private String provider = "SIMULATED";

    @Column(name = "external_message_id", nullable = false, length = 255)
    private String externalMessageId;

    @Column(name = "external_thread_id", length = 255)
    private String externalThreadId;

    @Column(name = "sender", nullable = false, length = 255)
    private String sender;

    @Column(name = "sender_domain", length = 255)
    private String senderDomain;

    @Column(name = "subject", length = 512)
    private String subject;

    @Column(name = "body_snippet", columnDefinition = "TEXT")
    private String bodySnippet;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "classification", length = 100)
    private String classification;

    @Column(name = "classification_confidence")
    private Double classificationConfidence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
