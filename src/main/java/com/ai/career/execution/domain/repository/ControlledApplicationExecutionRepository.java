package com.ai.career.execution.domain.repository;

import com.ai.career.execution.domain.entity.ControlledApplicationExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ControlledApplicationExecutionRepository extends JpaRepository<ControlledApplicationExecution, Long> {

    Optional<ControlledApplicationExecution> findByIdAndUserId(Long id, Long userId);

    List<ControlledApplicationExecution> findByApplicationIdAndUserIdOrderByCreatedAtDesc(Long applicationId, Long userId);

    Optional<ControlledApplicationExecution> findTopByApplicationIdAndUserIdOrderByCreatedAtDesc(Long applicationId, Long userId);

    Optional<ControlledApplicationExecution> findByExecutionRunId(String executionRunId);
}
