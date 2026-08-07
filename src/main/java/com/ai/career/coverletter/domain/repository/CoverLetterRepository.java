package com.ai.career.coverletter.domain.repository;

import com.ai.career.coverletter.domain.entity.CoverLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long> {

    List<CoverLetter> findByUserIdAndJobIdOrderByVersionDesc(Long userId, Long jobId);

    Optional<CoverLetter> findFirstByUserIdAndJobIdOrderByVersionDesc(Long userId, Long jobId);

    List<CoverLetter> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT MAX(cl.version) FROM CoverLetter cl WHERE cl.user.id = :userId AND cl.job.id = :jobId")
    Integer findMaxVersionByUserIdAndJobId(Long userId, Long jobId);
}
