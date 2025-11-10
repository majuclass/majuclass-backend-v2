package com.ssafy.a202.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 학생 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학생 수정 요청")
public class StudentUpdateRequest {

    @Schema(description = "학생 이름 (선택)", example = "김학생")
    @Size(max = 50, message = "학생 이름은 최대 50자입니다")
    private String name;

    @Schema(description = "담당 선생님 ID (선택, 변경하지 않으려면 null)", example = "2")
    private Long userId;
}