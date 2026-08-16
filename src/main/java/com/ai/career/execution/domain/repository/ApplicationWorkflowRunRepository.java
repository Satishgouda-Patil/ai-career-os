package com.ai.career.execution.domain.repository;

import com.ai.career.execution.domain.entity.ApplicationWorkflowRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationWorkflowRunRepository extends JpaRepository<ApplicationWorkflowRun, Long> {

    Optional<ApplicationWorkflowRun> findByIdempotencyKey(String idempotencyKey);

    List<ApplicationWorkflowRun> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);

    Optional<ApplicationWorkflowRun> findTopByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
