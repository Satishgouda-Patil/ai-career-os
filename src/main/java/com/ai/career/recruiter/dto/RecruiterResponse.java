package com.ai.career.recruiter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterResponse {
    private Long id;
    private Long companyId;
    private CompanyDto company;
    private String name;
    private String title;
    private String email;
    private String linkedinUrl;
    private String linkedin;
    private String location;
    private Integer confidenceScore;
    private String sourceProvider;
}
