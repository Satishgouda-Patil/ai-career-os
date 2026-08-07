package com.ai.career.jobanalysis.domain.entity;

import com.ai.career.domain.entity.Job;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false, unique = true)
    private Job job;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String responsibilities;

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;

    @Column(name = "preferred_skills", columnDefinition = "TEXT")
    private String preferredSkills;

    @Column(name = "salary_range", length = 100)
    private String salaryRange;

    @Column(name = "work_model", length = 50)
    private String workModel;

    @Column(name = "seniority_level", length = 50)
    private String seniorityLevel;

    @Column(name = "match_score", nullable = false)
    private Integer matchScore;

    @Column(name = "recommendation_status", length = 50)
    private String recommendationStatus;

    @Builder.Default
    @OneToMany(mappedBy = "jobAnalysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobMissingSkill> missingSkills = new ArrayList<>();

    @OneToOne(mappedBy = "jobAnalysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private JobRecommendation recommendation;

    @Column(name = "analyzed_at", insertable = false, updatable = false)
    private LocalDateTime analyzedAt;
}
