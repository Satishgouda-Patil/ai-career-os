package com.ai.career.tracking.interview.domain.repository;

import com.ai.career.tracking.interview.domain.entity.InterviewPreparation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterviewPreparationRepository extends JpaRepository<InterviewPreparation, Long> {

    Optional<InterviewPreparation> findTopByInterviewIdOrderByGeneratedAtDesc(Long interviewId);
}
