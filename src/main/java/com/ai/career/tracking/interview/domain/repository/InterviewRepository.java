package com.ai.career.tracking.interview.domain.repository;

import com.ai.career.tracking.interview.domain.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByApplicationIdOrderByScheduledAtDesc(Long applicationId);

    List<Interview> findByApplicationUserIdOrderByScheduledAtDesc(Long userId);
}
