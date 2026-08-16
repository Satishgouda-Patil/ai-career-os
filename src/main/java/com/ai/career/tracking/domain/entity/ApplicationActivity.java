package com.ai.career.tracking.domain.entity;

import com.ai.career.application.domain.entity.Application;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "activity_type", nullable = false, length = 100)
    private String activityType;

    @Column(name = "source", nullable = false, length = 50)
    @Builder.Default
    private String source = "SYSTEM";

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "confidence")
    @Builder.Default
    private Double confidence = 1.0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
