package com.ssafy.a202.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 정보 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 정보 수정 요청")
public class UserUpdateRequest {

    @Schema(description = "이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 최대 50자입니다")
    private String name;

    @Schema(description = "이메일", example = "teacher@school.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 200, message = "이메일은 최대 200자입니다")
    private String email;

    @Schema(description = "새 비밀번호 (선택, 변경하지 않으려면 null)", example = "newPassword123!")
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자 사이여야 합니다")
    private String newPassword;
}