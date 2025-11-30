package com.ssafy.a202.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "사용자 아이디", example = "teacher123", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @Schema(description = "비밀번호", example = "password1234!", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}