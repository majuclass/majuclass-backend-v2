package com.ssafy.a202.domain.scenariosession.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 세션 시작 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "세션 시작 요청")
public class SessionStartRequest {

    @NotNull(message = "학생 ID는 필수입니다")
    @Schema(description = "학생 ID", example = "1", required = true)
    private Long studentId;

    @NotNull(message = "시나리오 ID는 필수입니다")
    @Schema(description = "시나리오 ID", example = "1", required = true)
    private Long scenarioId;
}