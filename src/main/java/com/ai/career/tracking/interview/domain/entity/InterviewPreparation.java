package com.ai.career.tracking.interview.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_preparations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewPreparation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @Column(name = "company_overview_json", columnDefinition = "TEXT")
    private String companyOverviewJson;

    @Column(name = "role_focus_json", columnDefinition = "TEXT")
    private String roleFocusJson;

    @Column(name = "candidate_talking_points_json", columnDefinition = "TEXT")
    private String candidateTalkingPointsJson;

    @Column(name = "sample_questions_json", columnDefinition = "TEXT")
    private String sampleQuestionsJson;

    @Column(name = "questions_to_ask_json", columnDefinition = "TEXT")
    private String questionsToAskJson;

    @Column(name = "generated_at", insertable = false, updatable = false)
    private LocalDateTime generatedAt;
}
