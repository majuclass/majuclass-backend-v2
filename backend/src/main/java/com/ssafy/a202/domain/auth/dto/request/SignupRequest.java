package com.ssafy.a202.domain.auth.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "회원가입 요청")
public record SignupRequest(
        @Schema(description = "소속 기관 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long orgId,

        @Schema(description = "사용자 아이디", example = "teacher123", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @Schema(description = "비밀번호", example = "password1234!", requiredMode = Schema.RequiredMode.REQUIRED)
        String password,

        @Schema(description = "이름", example = "김선생", requiredMode = Schema.RequiredMode.REQUIRED)
        String fullName,

        @Schema(description = "이메일", example = "teacher@school.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email
){
}
