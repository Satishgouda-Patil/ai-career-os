package com.ai.career.match.service;

import java.util.List;

public interface MatchScoringService {
    void processJobMatching(List<Long> jobIds);
    void processProfileMatching(Long userId);
    int calculateMatchScore(SetOfSkills profileSkills, String jobTitle, String jobDescription);

    interface SetOfSkills {
        List<String> getSkillNames();
    }
}
