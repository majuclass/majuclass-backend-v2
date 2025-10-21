package com.ssafy.a202.domain.auth.service;

import com.ssafy.a202.domain.auth.dto.LoginRequest;
import com.ssafy.a202.domain.auth.dto.LoginResponse;

/**
 * 인증 서비스 인터페이스
 */
public interface AuthService {

    /**
     * 로그인
     */
    LoginResponse login(LoginRequest request);

}
