package com.ai.career.jobanalysis.domain.repository;

import com.ai.career.jobanalysis.domain.entity.JobMissingSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobMissingSkillRepository extends JpaRepository<JobMissingSkill, Long> {
    List<JobMissingSkill> findByJobAnalysisId(Long jobAnalysisId);
}
