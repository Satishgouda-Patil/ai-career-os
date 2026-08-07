package com.ai.career.resume.service;

import com.ai.career.resume.dto.GenerateResumeRequest;
import com.ai.career.resume.dto.ResumeResponse;

import java.util.List;

public interface ResumeService {
    ResumeResponse generateResume(Long userId, GenerateResumeRequest request);
    List<ResumeResponse> getResumeHistory(Long userId);
    ResumeResponse getResumeById(Long userId, Long resumeId);
    byte[] exportResumePdf(Long userId, Long resumeId);
    byte[] exportResumeDocx(Long userId, Long resumeId);
    void softDeleteResume(Long userId, Long resumeId);
}
