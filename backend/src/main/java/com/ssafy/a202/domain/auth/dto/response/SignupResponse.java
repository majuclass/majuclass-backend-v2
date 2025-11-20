package com.ssafy.a202.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 회원가입 응답 DTO
 */
@Schema(description = "회원가입 성공 응답 정보")
@Getter
@Builder
public class SignupResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자명", example = "teacher01")
    private String username;

    @Schema(description = "이름", example = "홍길동")
    private String name;
}