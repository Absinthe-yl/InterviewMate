package org.interviewmate.service;

import org.interviewmate.dto.LoginRequest;
import org.interviewmate.dto.LoginResponse;
import org.interviewmate.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    void logout(String token);
}
