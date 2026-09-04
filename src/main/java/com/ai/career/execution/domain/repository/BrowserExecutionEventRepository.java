package com.ai.career.execution.domain.repository;

import com.ai.career.execution.domain.entity.BrowserExecutionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrowserExecutionEventRepository extends JpaRepository<BrowserExecutionEvent, Long> {
    List<BrowserExecutionEvent> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
    List<BrowserExecutionEvent> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<BrowserExecutionEvent> findByExecutionRunIdOrderByCreatedAtDesc(Long executionRunId);
}
