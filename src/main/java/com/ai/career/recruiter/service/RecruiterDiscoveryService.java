package com.ai.career.recruiter.service;

import com.ai.career.recruiter.dto.CompanyDto;
import com.ai.career.recruiter.dto.DiscoverRecruiterRequest;
import com.ai.career.recruiter.dto.RecruiterResponse;

import java.util.List;

public interface RecruiterDiscoveryService {
    List<RecruiterResponse> discoverRecruiters(DiscoverRecruiterRequest request);
    List<RecruiterResponse> getRecruitersByCompanyId(Long companyId);
    RecruiterResponse getRecruiterById(Long recruiterId);
    CompanyDto getOrCreateCompany(String companyName);
}
