package com.ai.career.recruiter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoverRecruiterRequest {
    @NotNull(message = "Company ID is required")
    private Long companyId;
}
