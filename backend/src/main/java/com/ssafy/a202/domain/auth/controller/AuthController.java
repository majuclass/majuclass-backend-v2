package com.ssafy.a202.domain.auth.controller;

import com.ssafy.a202.common.entity.ApiResponse;
import com.ssafy.a202.common.entity.SuccessCode;
import com.ssafy.a202.domain.auth.dto.request.LoginRequest;
import com.ssafy.a202.domain.auth.dto.request.SignupRequest;
import com.ssafy.a202.domain.auth.dto.response.LoginResponse;
import com.ssafy.a202.domain.auth.dto.response.SignupResponse;
import com.ssafy.a202.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ApiResponse.success(SuccessCode.SIGNUP_SUCCESS, response);
    }

    @Operation(summary = "로그인", description = "사용자 로그인 후 JWT 토큰을 발급합니다.")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(SuccessCode.LOGIN_SUCCESS, response);
    }

}
