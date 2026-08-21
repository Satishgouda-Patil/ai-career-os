package com.ai.career.execution.sandbox.domain.repository;

import com.ai.career.execution.sandbox.domain.entity.SandboxExecutionRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SandboxExecutionRunRepository extends JpaRepository<SandboxExecutionRun, Long> {

    List<SandboxExecutionRun> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SandboxExecutionRun> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);

    Optional<SandboxExecutionRun> findTopByApplicationIdOrderByCreatedAtDesc(Long applicationId);

    Optional<SandboxExecutionRun> findTopByApplicationIdAndUserIdOrderByCreatedAtDesc(Long applicationId, Long userId);
}
