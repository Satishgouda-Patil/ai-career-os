package com.ai.career.recruiter.domain.repository;

import com.ai.career.recruiter.domain.entity.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {
    List<Recruiter> findByCompanyId(Long companyId);
    Optional<Recruiter> findByCompanyIdAndEmail(Long companyId, String email);
}
