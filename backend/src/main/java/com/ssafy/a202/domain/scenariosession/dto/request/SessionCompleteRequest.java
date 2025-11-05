package com.ssafy.a202.domain.scenariosession.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 세션 완료 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "세션 완료 요청")
public class SessionCompleteRequest {

    @NotNull(message = "세션 ID는 필수입니다")
    @Schema(description = "완료할 세션 ID", example = "1", required = true)
    private Long sessionId;
}