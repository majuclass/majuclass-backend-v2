package com.ssafy.a202.domain.scenariosession.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 답안 제출 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "답안 제출 요청")
public class AnswerSubmitRequest {

    @NotNull(message = "세션 ID는 필수입니다")
    @Schema(description = "세션 ID", example = "1")
    private Long sessionId;

    @NotNull(message = "시나리오 ID는 필수입니다")
    @Schema(description = "시나리오 ID", example = "1")
    private Long scenarioId;

    @NotNull(message = "시퀀스 번호는 필수입니다")
    @Schema(description = "시퀀스 번호 (1부터 시작)", example = "1")
    private Integer sequenceNumber;

    @NotNull(message = "선택한 옵션 ID는 필수입니다")
    @Schema(description = "선택한 옵션 ID", example = "3")
    private Long selectedOptionId;
}
