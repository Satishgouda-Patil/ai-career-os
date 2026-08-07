package com.ai.career.jobanalysis.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_missing_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobMissingSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_analysis_id", nullable = false)
    private JobAnalysis jobAnalysis;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(length = 50)
    private String priority;

    @Column(name = "learning_suggestion", length = 512)
    private String learningSuggestion;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
