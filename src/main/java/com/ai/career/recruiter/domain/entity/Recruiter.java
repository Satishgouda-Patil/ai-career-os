package com.ai.career.recruiter.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recruiters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recruiter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    private String title;

    private String email;

    @Column(name = "linkedin_url", length = 512)
    private String linkedinUrl;

    private String location;

    @Column(name = "confidence_score")
    private Integer confidenceScore;

    @Column(name = "source_provider", length = 50)
    private String sourceProvider;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
