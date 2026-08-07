package com.ai.career.resume.service;

import com.ai.career.resume.domain.entity.ResumeVersion;
import com.ai.career.resume.dto.AtsAnalysisResponse;

public interface AtsAnalysisService {
    AtsAnalysisResponse analyzeResume(ResumeVersion resumeVersion);
    AtsAnalysisResponse getAnalysisByResumeVersionId(Long resumeVersionId);
}
