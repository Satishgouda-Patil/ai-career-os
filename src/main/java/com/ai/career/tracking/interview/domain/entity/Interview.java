package com.ai.career.tracking.interview.domain.entity;

import com.ai.career.application.domain.entity.Application;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "interview_type", nullable = false, length = 100)
    @Builder.Default
    private String interviewType = "GENERAL";

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Column(name = "meeting_url", length = 512)
    private String meetingUrl;

    @Column(name = "interviewer_name", length = 255)
    private String interviewerName;

    @Column(name = "interviewer_title", length = 255)
    private String interviewerTitle;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "SCHEDULED";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
