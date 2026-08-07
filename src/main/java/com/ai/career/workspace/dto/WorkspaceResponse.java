package com.ai.career.workspace.dto;

import com.ai.career.communication.dto.EmailResponse;
import com.ai.career.coverletter.dto.CoverLetterResponse;
import com.ai.career.job.dto.JobDto;
import com.ai.career.jobanalysis.dto.JobAnalysisResponse;
import com.ai.career.jobanalysis.dto.JobRecommendationDto;
import com.ai.career.recruiter.dto.RecruiterResponse;
import com.ai.career.resume.dto.AtsAnalysisResponse;
import com.ai.career.resume.dto.ResumeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponse {
    private Long workspaceId;
    private String status;
    private boolean approved;
    private boolean rejected;
    private JobDto job;
    private JobAnalysisResponse analysis;
    private ResumeResponse resume;
    private AtsAnalysisResponse ats;
    private CoverLetterResponse coverLetter;
    private List<RecruiterResponse> recruiters;
    private EmailResponse email;
    private JobRecommendationDto recommendation;
}
