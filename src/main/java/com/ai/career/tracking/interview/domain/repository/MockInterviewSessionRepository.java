package com.ai.career.tracking.interview.domain.repository;

import com.ai.career.tracking.interview.domain.entity.MockInterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockInterviewSessionRepository extends JpaRepository<MockInterviewSession, Long> {

    List<MockInterviewSession> findByInterviewIdOrderByCreatedAtDesc(Long interviewId);
}
