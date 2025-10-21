package com.ssafy.a202.domain.auth.controller;

import com.ssafy.a202.domain.auth.dto.LoginRequest;
import com.ssafy.a202.domain.auth.dto.LoginResponse;
import com.ssafy.a202.domain.auth.service.AuthService;
import com.ssafy.a202.global.constants.SuccessCode;
import com.ssafy.a202.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 컨트롤러
 *
 * 역할 기반 권한 제어 사용 예시:
 * - @PreAuthorize("hasRole('ADMIN')")           // ADMIN만 접근 가능
 * - @PreAuthorize("hasRole('USER')")            // USER(선생님)만 접근 가능
 * - @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // USER 또는 ADMIN 접근 가능
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "사용자 로그인 후 JWT 토큰을 발급합니다.")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(SuccessCode.LOGIN_SUCCESS, response);
    }
}
