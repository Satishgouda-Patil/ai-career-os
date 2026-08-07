package com.ai.career.jobanalysis.service;

import com.ai.career.jobanalysis.dto.JobAnalysisResponse;
import com.ai.career.jobanalysis.dto.JobRecommendationDto;
import com.ai.career.jobanalysis.dto.MissingSkillDto;

import java.util.List;

public interface JobAnalysisService {
    JobAnalysisResponse analyzeJob(Long userId, Long jobId);
    JobAnalysisResponse getJobAnalysis(Long userId, Long jobId);
    List<MissingSkillDto> getMissingSkills(Long userId, Long jobId);
    JobRecommendationDto getJobRecommendation(Long userId, Long jobId);
}
