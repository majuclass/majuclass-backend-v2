package com.ssafy.a202.domain.auth.service;

import com.ssafy.a202.common.entity.CustomException.CustomException;
import com.ssafy.a202.common.exception.ErrorCode;
import com.ssafy.a202.common.security.JwtProvider;
import com.ssafy.a202.domain.auth.dto.request.LoginRequest;
import com.ssafy.a202.domain.auth.dto.request.SignupRequest;
import com.ssafy.a202.domain.auth.dto.response.LoginResponse;
import com.ssafy.a202.domain.auth.dto.response.SignupResponse;
import com.ssafy.a202.domain.organization.entity.Organization;
import com.ssafy.a202.domain.organization.repository.OrganizationRepository;
import com.ssafy.a202.domain.user.entity.User;
import com.ssafy.a202.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 1. 사용자명 중복 체크
        if (userRepository.existsByUsernameAndDeletedAtIsNull(request.username())) {
            throw new CustomException(ErrorCode.USERNAME_DUPLICATE);
        }

        // 2. 이메일 중복 체크
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATE);
        }

        // 3. 학교 조회
        Organization org = organizationRepository.findById(request.orgId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND));

        // 4. 비밀번호 암호화
        String encodePassword = passwordEncoder.encode(request.password());

        // 5. 사용자 엔티티 생성
        User user = User.of(org, request, encodePassword);

        // 6. 저장
        User savedUser = userRepository.save(user);

        log.info("New user registered: {}", savedUser.getUsername());

        // 7. 응답 생성
        return SignupResponse.of(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByUsernameAndDeletedAtIsNull(request.username())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 비밀번호 검증
        if(!passwordEncoder.matches(request.password(), user.getPassword())) {
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
        return LoginResponse.of(user, accessToken, refreshToken);
    }
}
