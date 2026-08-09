package com.ai.career.application.domain.repository;

import com.ai.career.application.domain.entity.ApplicationExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationExecutionRepository extends JpaRepository<ApplicationExecution, Long> {
    List<ApplicationExecution> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
