package com.ai.career.auth.service;

import com.ai.career.auth.dto.AuthResponse;
import com.ai.career.auth.dto.LoginRequest;
import com.ai.career.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
