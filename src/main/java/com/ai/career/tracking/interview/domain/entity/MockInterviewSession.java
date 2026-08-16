package com.ai.career.tracking.interview.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mock_interview_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "question_category", length = 100)
    @Builder.Default
    private String questionCategory = "TECHNICAL";

    @Column(name = "candidate_answer", columnDefinition = "TEXT")
    private String candidateAnswer;

    @Column(name = "score")
    private Integer score;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "improved_answer", columnDefinition = "TEXT")
    private String improvedAnswer;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
