package com.ai.career.validation.repository;

import com.ai.career.validation.entity.ApplicationDryRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationDryRunRepository extends JpaRepository<ApplicationDryRunEntity, Long> {
    Optional<ApplicationDryRunEntity> findByRunId(String runId);
    List<ApplicationDryRunEntity> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
