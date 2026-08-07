package com.ai.career.jobanalysis.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_analysis_id", nullable = false, unique = true)
    private JobAnalysis jobAnalysis;

    @Column(nullable = false, length = 50)
    private String decision;

    @Column(nullable = false)
    private Integer confidence;

    @Column(columnDefinition = "TEXT")
    private String rationale;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
