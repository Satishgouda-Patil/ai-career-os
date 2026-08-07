package com.ai.career.communication.domain.entity;

import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.recruiter.domain.entity.Recruiter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_drafts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id")
    private Recruiter recruiter;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(columnDefinition = "TEXT")
    private String followup;

    @Column(name = "linkedin_message", columnDefinition = "TEXT")
    private String linkedinMessage;

    @Column(length = 50)
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
