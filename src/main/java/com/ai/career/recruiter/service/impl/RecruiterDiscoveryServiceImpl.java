package com.ai.career.recruiter.service.impl;

import com.ai.career.recruiter.domain.entity.Company;
import com.ai.career.recruiter.domain.entity.Recruiter;
import com.ai.career.recruiter.domain.repository.CompanyRepository;
import com.ai.career.recruiter.domain.repository.RecruiterRepository;
import com.ai.career.recruiter.dto.CompanyDto;
import com.ai.career.recruiter.dto.DiscoverRecruiterRequest;
import com.ai.career.recruiter.dto.RecruiterResponse;
import com.ai.career.recruiter.provider.RecruiterDiscoveryProvider;
import com.ai.career.recruiter.service.RecruiterDiscoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruiterDiscoveryServiceImpl implements RecruiterDiscoveryService {

    private final CompanyRepository companyRepository;
    private final RecruiterRepository recruiterRepository;
    private final List<RecruiterDiscoveryProvider> discoveryProviders;

    @Override
    @Transactional
    public List<RecruiterResponse> discoverRecruiters(DiscoverRecruiterRequest request) {
        log.info("Discovering recruiters for Company ID: {}", request.getCompanyId());

        Company company = companyRepository.findById(request.getCompanyId())
            .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + request.getCompanyId()));

        for (RecruiterDiscoveryProvider provider : discoveryProviders) {
            try {
                List<Recruiter> discovered = provider.discoverRecruiters(company);
                for (Recruiter rec : discovered) {
                    if (rec.getEmail() != null && recruiterRepository.findByCompanyIdAndEmail(company.getId(), rec.getEmail()).isEmpty()) {
                        recruiterRepository.save(rec);
                    }
                }
            } catch (Exception e) {
                log.warn("Provider {} failed recruiter discovery: {}", provider.getProviderName(), e.getMessage());
            }
        }

        return getRecruitersByCompanyId(company.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecruiterResponse> getRecruitersByCompanyId(Long companyId) {
        return recruiterRepository.findByCompanyId(companyId)
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RecruiterResponse getRecruiterById(Long recruiterId) {
        Recruiter recruiter = recruiterRepository.findById(recruiterId)
            .orElseThrow(() -> new IllegalArgumentException("Recruiter not found with ID: " + recruiterId));
        return mapToDto(recruiter);
    }

    @Override
    @Transactional
    public CompanyDto getOrCreateCompany(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            companyName = "Default Company";
        }
        final String searchName = companyName;
        Company company = companyRepository.findByName(searchName)
            .orElseGet(() -> companyRepository.save(
                Company.builder()
                    .name(searchName)
                    .industry("Technology")
                    .website("https://" + searchName.toLowerCase().replaceAll("[^a-z0-9]", "") + ".com")
                    .location("United States")
                    .description("Technology company")
                    .build()
            ));
        return mapCompanyToDto(company);
    }

    private RecruiterResponse mapToDto(Recruiter entity) {
        return RecruiterResponse.builder()
            .id(entity.getId())
            .companyId(entity.getCompany().getId())
            .company(mapCompanyToDto(entity.getCompany()))
            .name(entity.getName())
            .title(entity.getTitle())
            .email(entity.getEmail())
            .linkedinUrl(entity.getLinkedinUrl())
            .linkedin(entity.getLinkedinUrl())
            .location(entity.getLocation())
            .confidenceScore(entity.getConfidenceScore())
            .sourceProvider(entity.getSourceProvider())
            .build();
    }

    private CompanyDto mapCompanyToDto(Company entity) {
        return CompanyDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .industry(entity.getIndustry())
            .website(entity.getWebsite())
            .location(entity.getLocation())
            .description(entity.getDescription())
            .build();
    }
}
