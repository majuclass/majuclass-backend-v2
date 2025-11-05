package com.ssafy.a202.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 갱신 요청 DTO
 */
@Schema(description = "토큰 갱신 요청 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "리프레시 토큰은 필수입니다")
    private String refreshToken;
}