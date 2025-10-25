package com.ssafy.a202.domain.scenariosession.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 답안 검증 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "답안 검증 응답")
public class AnswerCheckResponse {
    @Schema(description = "시나리오 ID", example = "1")
    private Long scenarioId;

    @Schema(description = "시퀀스 ID", example = "1")
    private Long sequenceId;

    @Schema(description = "시퀀스 번호", example = "1")
    private int sequenceNumber;

    @Schema(description = "제출한 옵션 ID", example = "3")
    private Long submittedOptionId;

    @Schema(description = "정답 여부", example = "true")
    private boolean isCorrect;
}
