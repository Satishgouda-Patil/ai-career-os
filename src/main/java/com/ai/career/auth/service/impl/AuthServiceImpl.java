package com.ai.career.auth.service.impl;

import com.ai.career.auth.dto.AuthResponse;
import com.ai.career.auth.dto.LoginRequest;
import com.ai.career.auth.dto.RegisterRequest;
import com.ai.career.auth.service.AuthService;
import com.ai.career.domain.entity.Profile;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.ProfileRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.security.JwtTokenProvider;
import com.ai.career.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email address already in use.");
        }

        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role("USER")
            .build();

        User savedUser = userRepository.save(user);

        Profile profile = Profile.builder()
            .user(savedUser)
            .fullName(request.getFullName())
            .build();

        profileRepository.save(profile);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String jwt = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
            .token(jwt)
            .userId(savedUser.getId())
            .email(savedUser.getEmail())
            .fullName(profile.getFullName())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        Profile profile = profileRepository.findById(principal.getId()).orElse(null);
        String fullName = (profile != null) ? profile.getFullName() : null;

        return AuthResponse.builder()
            .token(jwt)
            .userId(principal.getId())
            .email(principal.getEmail())
            .fullName(fullName)
            .build();
    }
}
