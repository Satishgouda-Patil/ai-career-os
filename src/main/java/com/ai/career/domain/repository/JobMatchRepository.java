package com.ai.career.domain.repository;

import com.ai.career.domain.entity.JobMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobMatchRepository extends JpaRepository<JobMatch, Long> {

    Optional<JobMatch> findByProfileUserIdAndJobId(Long profileId, Long jobId);

    @Query("SELECT jm FROM JobMatch jm JOIN FETCH jm.job WHERE jm.profile.userId = :profileId AND jm.score >= :minScore ORDER BY jm.score DESC")
    List<JobMatch> findMatchedJobsForProfile(@Param("profileId") Long profileId, @Param("minScore") Integer minScore);
}
