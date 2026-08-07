package com.ai.career.jobanalysis.domain.repository;

import com.ai.career.jobanalysis.domain.entity.JobAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobAnalysisRepository extends JpaRepository<JobAnalysis, Long> {
    Optional<JobAnalysis> findByJobId(Long jobId);
}
