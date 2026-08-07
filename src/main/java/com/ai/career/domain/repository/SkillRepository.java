package com.ai.career.domain.repository;

import com.ai.career.domain.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Integer> {
    Optional<Skill> findByNameIgnoreCase(String name);
    Set<Skill> findByNameInIgnoreCase(List<String> names);
}
