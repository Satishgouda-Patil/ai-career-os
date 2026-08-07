package com.ai.career.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private Long userId;
    private String email;
    private String fullName;
    private String summary;
    private String location;
    private String resumeUrl;
    private Set<SkillDto> skills;
}
