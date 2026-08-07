package com.ai.career.jobanalysis.domain.repository;

import com.ai.career.jobanalysis.domain.entity.JobRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRecommendationRepository extends JpaRepository<JobRecommendation, Long> {
    Optional<JobRecommendation> findByJobAnalysisId(Long jobAnalysisId);
}
