package com.ssafy.a202.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 */
@Schema(description = "로그인 요청 정보")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Schema(description = "사용자 ID", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "사용자 ID는 필수입니다")
    @Size(min = 3, max = 50, message = "사용자명은 3~50자 사이여야 합니다")
    private String username;

    @Schema(description = "비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자 사이여야 합니다")
    private String password;
}