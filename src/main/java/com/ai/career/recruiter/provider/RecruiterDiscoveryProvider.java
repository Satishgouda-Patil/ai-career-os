package com.ai.career.recruiter.provider;

import com.ai.career.recruiter.domain.entity.Company;
import com.ai.career.recruiter.domain.entity.Recruiter;

import java.util.List;

public interface RecruiterDiscoveryProvider {
    String getProviderName();
    List<Recruiter> discoverRecruiters(Company company);
}
