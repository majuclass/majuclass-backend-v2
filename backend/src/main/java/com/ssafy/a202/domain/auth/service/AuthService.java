package com.ssafy.a202.domain.auth.service;

import com.ssafy.a202.domain.auth.dto.LoginRequest;
import com.ssafy.a202.domain.auth.dto.LoginResponse;
import com.ssafy.a202.domain.auth.dto.SignupRequest;
import com.ssafy.a202.domain.auth.dto.SignupResponse;

/**
 * 인증 서비스 인터페이스
 */
public interface AuthService {

    /**
     * 회원가입
     */
    SignupResponse signup(SignupRequest request);

    /**
     * 로그인
     */
    LoginResponse login(LoginRequest request);

    /**
     * 로그아웃
     */
    void logout(String refreshToken);

    /**
     * 토큰 갱신
     */
    LoginResponse refreshToken(String refreshToken);
}
