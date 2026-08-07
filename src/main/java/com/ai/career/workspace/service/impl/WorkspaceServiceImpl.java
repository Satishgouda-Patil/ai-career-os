package com.ai.career.workspace.service.impl;

import com.ai.career.communication.dto.EmailResponse;
import com.ai.career.communication.dto.GenerateEmailRequest;
import com.ai.career.communication.service.EmailGeneratorService;
import com.ai.career.coverletter.dto.CoverLetterResponse;
import com.ai.career.coverletter.dto.GenerateCoverLetterRequest;
import com.ai.career.coverletter.service.CoverLetterService;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.job.dto.JobDto;
import com.ai.career.jobanalysis.dto.JobAnalysisResponse;
import com.ai.career.jobanalysis.service.JobAnalysisService;
import com.ai.career.recruiter.dto.CompanyDto;
import com.ai.career.recruiter.dto.DiscoverRecruiterRequest;
import com.ai.career.recruiter.dto.RecruiterResponse;
import com.ai.career.recruiter.service.RecruiterDiscoveryService;
import com.ai.career.resume.dto.GenerateResumeRequest;
import com.ai.career.resume.dto.ResumeResponse;
import com.ai.career.resume.service.ResumeService;
import com.ai.career.workspace.domain.entity.Workspace;
import com.ai.career.workspace.domain.repository.WorkspaceRepository;
import com.ai.career.workspace.dto.WorkspaceResponse;
import com.ai.career.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final WorkspaceRepository workspaceRepository;
    private final JobAnalysisService jobAnalysisService;
    private final ResumeService resumeService;
    private final CoverLetterService coverLetterService;
    private final RecruiterDiscoveryService recruiterDiscoveryService;
    private final EmailGeneratorService emailGeneratorService;

    @Override
    @Transactional
    public WorkspaceResponse buildWorkspace(Long userId, Long jobId) {
        log.info("Building Unified AI Workspace for User ID: {}, Job ID: {}", userId, jobId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + jobId));

        Workspace workspace = workspaceRepository.findByUserIdAndJobId(userId, jobId)
            .orElseGet(() -> workspaceRepository.save(
                Workspace.builder()
                    .user(user)
                    .job(job)
                    .status("BUILDING")
                    .approved(false)
                    .rejected(false)
                    .build()
            ));

        JobAnalysisResponse analysis = jobAnalysisService.analyzeJob(userId, jobId);

        GenerateResumeRequest resumeReq = GenerateResumeRequest.builder().jobId(jobId).template("MODERN").build();
        ResumeResponse resume = resumeService.generateResume(userId, resumeReq);

        GenerateCoverLetterRequest clReq = GenerateCoverLetterRequest.builder().jobId(jobId).tone("Professional").build();
        CoverLetterResponse coverLetter = coverLetterService.generateCoverLetter(userId, clReq);

        CompanyDto company = recruiterDiscoveryService.getOrCreateCompany(job.getCompany());
        List<RecruiterResponse> recruiters = recruiterDiscoveryService.discoverRecruiters(
            DiscoverRecruiterRequest.builder().companyId(company.getId()).build()
        );

        Long recruiterId = (recruiters != null && !recruiters.isEmpty()) ? recruiters.get(0).getId() : null;
        GenerateEmailRequest emailReq = GenerateEmailRequest.builder().jobId(jobId).recruiterId(recruiterId).build();
        EmailResponse email = emailGeneratorService.generateColdEmail(userId, emailReq);

        workspace.setStatus("READY");
        workspaceRepository.save(workspace);

        return mapToDto(workspace, job, analysis, resume, coverLetter, recruiters, email);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(Long userId, Long jobId) {
        Workspace workspace = workspaceRepository.findByUserIdAndJobId(userId, jobId)
            .orElseGet(() -> {
                buildWorkspace(userId, jobId);
                return workspaceRepository.findByUserIdAndJobId(userId, jobId)
                    .orElseThrow(() -> new IllegalArgumentException("Could not build workspace for Job ID: " + jobId));
            });

        Job job = workspace.getJob();
        JobAnalysisResponse analysis = jobAnalysisService.getJobAnalysis(userId, jobId);
        ResumeResponse resume = resumeService.getResumeHistory(userId).stream().findFirst().orElse(null);
        CoverLetterResponse coverLetter = coverLetterService.getLatestCoverLetterByJobId(userId, jobId);

        CompanyDto company = recruiterDiscoveryService.getOrCreateCompany(job.getCompany());
        List<RecruiterResponse> recruiters = recruiterDiscoveryService.getRecruitersByCompanyId(company.getId());
        EmailResponse email = emailGeneratorService.getLatestEmailByJobId(userId, jobId);

        return mapToDto(workspace, job, analysis, resume, coverLetter, recruiters, email);
    }

    @Override
    @Transactional
    public WorkspaceResponse approveWorkspace(Long userId, Long jobId) {
        Workspace workspace = workspaceRepository.findByUserIdAndJobId(userId, jobId)
            .orElseThrow(() -> new IllegalArgumentException("Workspace not found for Job ID: " + jobId));

        workspace.setApproved(true);
        workspace.setRejected(false);
        workspace.setStatus("APPROVED");
        workspaceRepository.save(workspace);

        return getWorkspace(userId, jobId);
    }

    @Override
    @Transactional
    public WorkspaceResponse rejectWorkspace(Long userId, Long jobId) {
        Workspace workspace = workspaceRepository.findByUserIdAndJobId(userId, jobId)
            .orElseThrow(() -> new IllegalArgumentException("Workspace not found for Job ID: " + jobId));

        workspace.setApproved(false);
        workspace.setRejected(true);
        workspace.setStatus("REJECTED");
        workspaceRepository.save(workspace);

        return getWorkspace(userId, jobId);
    }

    @Override
    @Transactional
    public WorkspaceResponse regenerateWorkspace(Long userId, Long jobId) {
        return buildWorkspace(userId, jobId);
    }

    private WorkspaceResponse mapToDto(
        Workspace workspace,
        Job job,
        JobAnalysisResponse analysis,
        ResumeResponse resume,
        CoverLetterResponse coverLetter,
        List<RecruiterResponse> recruiters,
        EmailResponse email
    ) {
        JobDto jobDto = JobDto.builder()
            .id(job.getId())
            .source(job.getSource())
            .sourceJobId(job.getSourceJobId())
            .title(job.getTitle())
            .company(job.getCompany())
            .location(job.getLocation())
            .description(job.getDescription())
            .url(job.getUrl())
            .postedAt(job.getPostedAt())
            .build();

        return WorkspaceResponse.builder()
            .workspaceId(workspace.getId())
            .status(workspace.getStatus())
            .approved(workspace.isApproved())
            .rejected(workspace.isRejected())
            .job(jobDto)
            .analysis(analysis)
            .resume(resume)
            .ats(resume != null ? resume.getAnalysis() : null)
            .coverLetter(coverLetter)
            .recruiters(recruiters)
            .email(email)
            .recommendation(analysis != null ? analysis.getRecommendationDetails() : null)
            .build();
    }
}
