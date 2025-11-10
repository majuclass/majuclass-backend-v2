package com.ssafy.a202.domain.user.service;

import com.ssafy.a202.domain.auth.service.TokenBlacklistService;
import com.ssafy.a202.domain.user.dto.request.UserUpdateRequest;
import com.ssafy.a202.domain.user.dto.response.UserResponse;
import com.ssafy.a202.domain.user.dto.response.UserUpdateResponse;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtProvider jwtProvider;

    /**
     * 같은 학교 사용자 목록 조회
     */
    @Override
    public List<UserResponse> getUsers(Long userId) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 같은 학교 사용자 목록 조회
        List<User> users = userRepository.findBySchoolAndIsDeletedFalse(user.getSchool());

        log.debug("User list retrieved: userId={}, schoolId={}, count={}",
                userId, user.getSchool().getId(), users.size());

        // 3. 응답 생성
        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 상세 조회
     */
    @Override
    public UserResponse getUser(Long userId, Long targetUserId) {
        // 1. 요청 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 대상 사용자 조회
        User targetUser = userRepository.findByIdAndIsDeletedFalse(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3. 같은 학교인지 확인
        if (!user.getSchool().getId().equals(targetUser.getSchool().getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        log.debug("User detail retrieved: userId={}, targetUserId={}",
                userId, targetUserId);

        // 4. 응답 생성
        return UserResponse.from(targetUser);
    }

    /**
     * 회원 정보 수정 (본인만 가능)
     */
    @Override
    @Transactional
    public UserUpdateResponse updateUser(Long userId, UserUpdateRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 이메일 중복 체크 (본인 제외)
        if (!user.getEmail().equals(request.getEmail())) {
            boolean emailExists = userRepository.existsByEmailAndIsDeletedFalse(request.getEmail());
            if (emailExists) {
                throw new CustomException(ErrorCode.EMAIL_DUPLICATE);
            }
        }

        // 3. 기본 정보 수정
        user.updateInfo(request.getName(), request.getEmail());

        // 4. 비밀번호 변경 (제공된 경우만)
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.updatePassword(encodedPassword);
        }

        log.info("User updated: userId={}, username={}", user.getId(), user.getUsername());

        // 5. 응답 생성
        return UserUpdateResponse.from(user);
    }

    /**
     * 회원 탈퇴 (본인만 가능, soft delete)
     */
    @Override
    @Transactional
    public void withdrawUser(Long userId, String accessToken, String refreshToken) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 액세스 토큰 블랙리스트 처리
        try {
            jwtProvider.validateToken(accessToken);
            long accessTokenTtl = jwtProvider.getRemainingExpirationTimeInSeconds(accessToken);
            if (accessTokenTtl > 0) {
                tokenBlacklistService.addBlacklist(accessToken, accessTokenTtl);
                log.debug("Access token blacklisted for user: {}", user.getUsername());
            }
        } catch (JwtException e) {
            log.warn("Failed to blacklist access token: {}", e.getMessage());
        }

        // 3. 리프레시 토큰 블랙리스트 처리
        try {
            jwtProvider.validateToken(refreshToken);
            long refreshTokenTtl = jwtProvider.getRemainingExpirationTimeInSeconds(refreshToken);
            if (refreshTokenTtl > 0) {
                tokenBlacklistService.addBlacklist(refreshToken, refreshTokenTtl);
                log.debug("Refresh token blacklisted for user: {}", user.getUsername());
            }
        } catch (JwtException e) {
            log.warn("Failed to blacklist refresh token: {}", e.getMessage());
        }

        // 4. 삭제 처리
        user.delete();

        log.info("User withdrew and tokens blacklisted: userId={}, username={}", user.getId(), user.getUsername());
    }

    /**
     * 관리자가 사용자 삭제 (soft delete)
     */
    @Override
    @Transactional
    public void deleteUserByAdmin(Long userId) {
        // 1. 사용자 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 삭제 처리
        user.delete();

        log.info("User deleted by admin: userId={}, username={}", user.getId(), user.getUsername());
    }
}