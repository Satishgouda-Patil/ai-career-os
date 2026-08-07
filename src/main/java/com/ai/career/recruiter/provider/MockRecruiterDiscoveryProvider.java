package com.ai.career.recruiter.provider;

import com.ai.career.recruiter.domain.entity.Company;
import com.ai.career.recruiter.domain.entity.Recruiter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockRecruiterDiscoveryProvider implements RecruiterDiscoveryProvider {

    @Override
    public String getProviderName() {
        return "HUNTER_IO_MOCK";
    }

    @Override
    public List<Recruiter> discoverRecruiters(Company company) {
        String domain = company.getName().toLowerCase().replaceAll("[^a-z0-9]", "") + ".com";
        return List.of(
            Recruiter.builder()
                .company(company)
                .name("John Doe")
                .title("Senior Technical Recruiter")
                .email("john.doe@" + domain)
                .linkedinUrl("https://linkedin.com/in/john-doe-recruiter")
                .location(company.getLocation() != null ? company.getLocation() : "San Francisco, CA")
                .confidenceScore(92)
                .sourceProvider(getProviderName())
                .build(),
            Recruiter.builder()
                .company(company)
                .name("Sarah Smith")
                .title("Talent Acquisition Manager")
                .email("sarah.smith@" + domain)
                .linkedinUrl("https://linkedin.com/in/sarah-smith-ta")
                .location(company.getLocation() != null ? company.getLocation() : "New York, NY")
                .confidenceScore(88)
                .sourceProvider(getProviderName())
                .build()
        );
    }
}
