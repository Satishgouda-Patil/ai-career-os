package com.ai.career.coverletter.domain.entity;

import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cover_letters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoverLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false)
    private Integer version;

    @Column(length = 50)
    private String tone;

    @Column(length = 50)
    private String status;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
