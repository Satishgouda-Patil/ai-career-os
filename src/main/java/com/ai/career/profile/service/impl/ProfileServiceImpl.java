package com.ai.career.profile.service.impl;

import com.ai.career.domain.entity.Profile;
import com.ai.career.domain.entity.Skill;
import com.ai.career.domain.repository.ProfileRepository;
import com.ai.career.domain.repository.SkillRepository;
import com.ai.career.profile.dto.ProfileDto;
import com.ai.career.profile.dto.SkillDto;
import com.ai.career.profile.dto.UpdateProfileRequest;
import com.ai.career.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final SkillRepository skillRepository;

    @Override
    @Transactional(readOnly = true)
    public ProfileDto getProfile(Long userId) {
        Profile profile = profileRepository.findByUserIdWithSkills(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user ID: " + userId));

        return mapToDto(profile);
    }

    @Override
    @Transactional
    public ProfileDto updateProfile(Long userId, UpdateProfileRequest request) {
        Profile profile = profileRepository.findByUserIdWithSkills(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user ID: " + userId));

        profile.setFullName(request.getFullName());
        profile.setSummary(request.getSummary());
        profile.setLocation(request.getLocation());

        if (request.getSkills() != null) {
            Set<Skill> updatedSkills = new HashSet<>();
            for (String skillName : request.getSkills()) {
                Skill skill = skillRepository.findByNameIgnoreCase(skillName.trim())
                    .orElseGet(() -> skillRepository.save(Skill.builder().name(skillName.trim()).build()));
                updatedSkills.add(skill);
            }
            profile.setSkills(updatedSkills);
        }

        Profile savedProfile = profileRepository.save(profile);
        return mapToDto(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillDto> getAllSkills() {
        return skillRepository.findAll().stream()
            .map(s -> SkillDto.builder().id(s.getId()).name(s.getName()).build())
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateResumeUrl(Long userId, String resumeUrl) {
        Profile profile = profileRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user ID: " + userId));
        profile.setResumeUrl(resumeUrl);
        profileRepository.save(profile);
    }

    private ProfileDto mapToDto(Profile profile) {
        Set<SkillDto> skillDtos = profile.getSkills().stream()
            .map(s -> SkillDto.builder().id(s.getId()).name(s.getName()).build())
            .collect(Collectors.toSet());

        return ProfileDto.builder()
            .userId(profile.getUserId())
            .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
            .fullName(profile.getFullName())
            .summary(profile.getSummary())
            .location(profile.getLocation())
            .resumeUrl(profile.getResumeUrl())
            .skills(skillDtos)
            .build();
    }
}
