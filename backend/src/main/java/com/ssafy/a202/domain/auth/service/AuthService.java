package com.ssafy.a202.domain.auth.service;

import com.ssafy.a202.domain.auth.dto.request.LoginRequest;
import com.ssafy.a202.domain.auth.dto.request.SignupRequest;
import com.ssafy.a202.domain.auth.dto.response.LoginResponse;
import com.ssafy.a202.domain.auth.dto.response.SignupResponse;

public interface AuthService {

    SignupResponse signup(SignupRequest request);

    LoginResponse login(LoginRequest request);
}
