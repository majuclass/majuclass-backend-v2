package com.ssafy.a202.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 학생 추가 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학생 추가 요청")
public class StudentCreateRequest {

    @Schema(description = "학생 이름", example = "김학생", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "학생 이름은 필수입니다")
    @Size(max = 50, message = "학생 이름은 최대 50자입니다")
    private String name;
}