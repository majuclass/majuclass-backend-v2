package com.ssafy.a202.domain.auth.service;

import com.ssafy.a202.domain.auth.dto.LoginRequest;
import com.ssafy.a202.domain.auth.dto.LoginResponse;
import com.ssafy.a202.domain.user.entity.User;
import com.ssafy.a202.domain.user.repository.UserRepository;
import com.ssafy.a202.global.constants.ErrorCode;
import com.ssafy.a202.global.exception.CustomException;
import com.ssafy.a202.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    // todo: 회원가입 기능 적용 전까지 주석처리.
    //    private final SecurityConfig securityConfig;
    private final JwtProvider jwtProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // todo: 회원가입 기능 적용 전까지 주석처리.
        // 2. 비밀번호 검증
//        if (!securityConfig.passwordEncoder().matches(request.getPassword(), user.getPassword())) {
//            throw new CustomException(ErrorCode.INVALID_PASSWORD);
//        }

        // 3. JWT 토큰 생성
        String accessToken = jwtProvider.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        String refreshToken = jwtProvider.generateRefreshToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        // 4. 응답 생성
        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
