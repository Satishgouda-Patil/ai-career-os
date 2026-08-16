package com.ai.career.tracking.email.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_classification_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailClassificationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "email_message_id", nullable = false)
    private EmailMessage emailMessage;

    @Column(name = "classification", nullable = false, length = 100)
    private String classification;

    @Column(name = "confidence", nullable = false)
    private Double confidence;

    @Column(name = "extracted_data_json", columnDefinition = "TEXT")
    private String extractedDataJson;

    @Column(name = "model", length = 100)
    @Builder.Default
    private String model = "HEURISTIC_RULE_ENGINE";

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
