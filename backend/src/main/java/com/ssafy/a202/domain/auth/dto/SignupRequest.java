package com.ssafy.a202.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 */
@Schema(description = "회원가입 요청 정보")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @Schema(description = "학교 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "학교 ID는 필수입니다")
    private Long schoolId;

    @Schema(description = "사용자 ID", example = "teacher01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "사용자 ID는 필수입니다")
    @Size(min = 3, max = 50, message = "사용자 ID는 3~50자 사이여야 합니다")
    private String username;

    @Schema(description = "비밀번호", example = "password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자 사이여야 합니다")
    private String password;

    @Schema(description = "이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 최대 50자입니다")
    private String name;

    @Schema(description = "이메일", example = "teacher@school.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 200, message = "이메일은 최대 200자입니다")
    private String email;
}