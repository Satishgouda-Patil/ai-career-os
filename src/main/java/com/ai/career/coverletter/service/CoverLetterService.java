package com.ai.career.coverletter.service;

import com.ai.career.coverletter.dto.CoverLetterResponse;
import com.ai.career.coverletter.dto.GenerateCoverLetterRequest;

import java.util.List;

public interface CoverLetterService {
    CoverLetterResponse generateCoverLetter(Long userId, GenerateCoverLetterRequest request);
    CoverLetterResponse getLatestCoverLetterByJobId(Long userId, Long jobId);
    CoverLetterResponse regenerateCoverLetter(Long userId, Long jobId);
    List<CoverLetterResponse> getCoverLetterHistory(Long userId);
}
