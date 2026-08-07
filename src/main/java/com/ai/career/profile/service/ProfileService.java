package com.ai.career.profile.service;

import com.ai.career.profile.dto.ProfileDto;
import com.ai.career.profile.dto.SkillDto;
import com.ai.career.profile.dto.UpdateProfileRequest;

import java.util.List;

public interface ProfileService {
    ProfileDto getProfile(Long userId);
    ProfileDto updateProfile(Long userId, UpdateProfileRequest request);
    List<SkillDto> getAllSkills();
    void updateResumeUrl(Long userId, String resumeUrl);
}
