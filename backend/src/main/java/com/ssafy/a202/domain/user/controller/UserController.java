package com.ssafy.a202.domain.user.controller;

import com.ssafy.a202.domain.user.dto.request.UserUpdateRequest;
import com.ssafy.a202.domain.user.dto.request.WithdrawRequest;
import com.ssafy.a202.domain.user.dto.response.UserUpdateResponse;
import com.ssafy.a202.domain.user.service.UserService;
import com.ssafy.a202.global.constants.ErrorCode;
import com.ssafy.a202.global.constants.SuccessCode;
import com.ssafy.a202.global.exception.CustomException;
import com.ssafy.a202.global.response.ApiResponse;
import com.ssafy.a202.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원 정보 수정", description = "본인의 회원 정보(이름, 이메일, 비밀번호)를 수정합니다.")
    @PutMapping("/update")
    public ApiResponse<UserUpdateResponse> updateUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid UserUpdateRequest request) {
        UserUpdateResponse response = userService.updateUser(userPrincipal.getUserId(), request);
        return ApiResponse.success(SuccessCode.USER_UPDATE_SUCCESS, response);
    }

    @Operation(summary = "회원 탈퇴", description = "본인의 계정을 탈퇴합니다. (Soft Delete) 액세스 토큰과 리프레시 토큰이 모두 블랙리스트에 등록됩니다.")
    @DeleteMapping("/withdraw")
    public ApiResponse<Void> withdrawUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid WithdrawRequest request,
            HttpServletRequest httpRequest) {
        // Authorization 헤더에서 액세스 토큰 추출
        String accessToken = extractTokenFromHeader(httpRequest);

        userService.withdrawUser(userPrincipal.getUserId(), accessToken, request.getRefreshToken());
        return ApiResponse.success(SuccessCode.USER_DELETE_SUCCESS);
    }

    /**
     * HTTP 요청 헤더에서 Bearer 토큰 추출
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new CustomException(ErrorCode.TOKEN_INVALID);
    }

    @Operation(summary = "사용자 삭제 (관리자)", description = "관리자가 특정 사용자를 삭제합니다. (Soft Delete)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}/delete")
    public ApiResponse<Void> deleteUserByAdmin(
            @Parameter(description = "삭제할 사용자 ID", example = "1")
            @PathVariable Long userId) {
        userService.deleteUserByAdmin(userId);
        return ApiResponse.success(SuccessCode.USER_DELETE_SUCCESS);
    }
}