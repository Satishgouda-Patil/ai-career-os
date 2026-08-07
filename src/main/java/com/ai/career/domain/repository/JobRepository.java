package com.ai.career.domain.repository;

import com.ai.career.domain.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findBySourceAndSourceJobId(String source, String sourceJobId);
    boolean existsBySourceAndSourceJobId(String source, String sourceJobId);
    boolean existsByTitleAndCompanyAndLocation(String title, String company, String location);
}
