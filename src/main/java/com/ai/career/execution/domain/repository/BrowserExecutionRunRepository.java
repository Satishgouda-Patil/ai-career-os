package com.ai.career.execution.domain.repository;

import com.ai.career.execution.domain.entity.BrowserExecutionRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrowserExecutionRunRepository extends JpaRepository<BrowserExecutionRun, Long> {
    List<BrowserExecutionRun> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
    List<BrowserExecutionRun> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<BrowserExecutionRun> findTopByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
