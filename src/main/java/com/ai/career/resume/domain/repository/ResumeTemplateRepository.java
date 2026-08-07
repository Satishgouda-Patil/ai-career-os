package com.ai.career.resume.domain.repository;

import com.ai.career.resume.domain.entity.ResumeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeTemplateRepository extends JpaRepository<ResumeTemplate, Integer> {
    Optional<ResumeTemplate> findByName(String name);
}
