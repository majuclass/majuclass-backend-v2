package com.ssafy.a202.domain.auth.service;

import com.ssafy.a202.domain.auth.dto.LoginRequest;
import com.ssafy.a202.domain.auth.dto.LoginResponse;
import com.ssafy.a202.domain.auth.dto.SignupRequest;
import com.ssafy.a202.domain.auth.dto.SignupResponse;
import com.ssafy.a202.domain.school.entity.School;
import com.ssafy.a202.domain.school.repository.SchoolRepository;
import com.ssafy.a202.domain.user.entity.User;
import com.ssafy.a202.domain.user.repository.UserRepository;
import com.ssafy.a202.global.constants.ErrorCode;
import com.ssafy.a202.global.exception.CustomException;
import com.ssafy.a202.global.security.JwtProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 회원가입
     */
    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 1. 사용자명 중복 체크
        if (userRepository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
            throw new CustomException(ErrorCode.USERNAME_DUPLICATE);
        }

        // 2. 이메일 중복 체크
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATE);
        }

        // 3. 학교 조회
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCHOOL_NOT_FOUND));

        // 4. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 5. 사용자 엔티티 생성
        User user = User.builder()
                .school(school)
                .username(request.getUsername())
                .password(encodedPassword)
                .name(request.getName())
                .email(request.getEmail())
                .build();

        // 6. 저장
        User savedUser = userRepository.save(user);

        log.info("New user registered: {}", savedUser.getUsername());

        // 7. 응답 생성
        return SignupResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .name(savedUser.getName())
                .build();
    }

    /**
     * 로그인
     * JWT 토큰을 생성하여 반환하면, 이후 요청에서는 JwtAuthenticationFilter가
     * 토큰을 검증하고 UserPrincipal을 생성하여 SecurityContext에 저장
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByUsernameAndIsDeletedFalse(request.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

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

        // 토큰 정보 추출
        String username = jwtProvider.getUsernameFromToken(refreshToken);

        log.info("Login Successfully with username: {}", username);

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

    /**
     * 로그아웃
     * 리프레시 토큰을 블랙리스트에 추가하여 무효화
     */
    @Override
    public void logout(String refreshToken) {
        // JWT 토큰 유효성 검증
        try {
            jwtProvider.validateToken(refreshToken);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        // 토큰 타입 확인 (refresh 토큰만 허용)
        String tokenType = jwtProvider.getTokenTypeFromToken(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        // 토큰 정보 추출
        String username = jwtProvider.getUsernameFromToken(refreshToken);

        // 토큰을 블랙리스트에 추가 (남은 만료시간 만큼 TTL 설정)
        long remainingTimeSeconds = jwtProvider.getRemainingExpirationTimeInSeconds(refreshToken);
        if (remainingTimeSeconds > 0) {
            tokenBlacklistService.addBlacklist(refreshToken, remainingTimeSeconds);
        }

        log.info("User logged out and token blacklisted: {}", username);
    }

    /**
     * 토큰 갱신
     * 기존 리프레시 토큰을 블랙리스트에 추가하여 재사용 방지
     */
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // 1. 리프레시 토큰 검증
        try {
            jwtProvider.validateToken(refreshToken);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        // 토큰 타입 확인 (refresh 토큰만 허용)
        String tokenType = jwtProvider.getTokenTypeFromToken(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        // 2. 토큰에서 사용자 정보 추출
        Long userId = jwtProvider.getUserIdFromToken(refreshToken);
        String username = jwtProvider.getUsernameFromToken(refreshToken);

        // 3. 사용자 존재 여부 확인
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 4. 기존 리프레시 토큰을 블랙리스트에 추가 (재사용 방지)
        long remainingTimeSeconds = jwtProvider.getRemainingExpirationTimeInSeconds(refreshToken);
        if (remainingTimeSeconds > 0) {
            tokenBlacklistService.addBlacklist(refreshToken, remainingTimeSeconds);
        }

        // 5. 새로운 토큰 생성
        String newAccessToken = jwtProvider.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        String newRefreshToken = jwtProvider.generateRefreshToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        log.info("Token refreshed for user: {} (old token blacklisted)", username);

        // 6. 응답 생성
        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().name())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
