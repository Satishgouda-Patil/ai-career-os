package com.ai.career.resume.domain.repository;

import com.ai.career.resume.domain.entity.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, Long> {

    List<ResumeVersion> findByUserIdAndDeletedFalseOrderByVersionDesc(Long userId);

    Optional<ResumeVersion> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    @Query("SELECT MAX(rv.version) FROM ResumeVersion rv WHERE rv.user.id = :userId")
    Integer findMaxVersionByUserId(Long userId);

    List<ResumeVersion> findByUserIdAndJobIdAndDeletedFalseOrderByVersionDesc(Long userId, Long jobId);
}
