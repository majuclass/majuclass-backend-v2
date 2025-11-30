package com.ssafy.a202.domain.session.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.ssafy.a202.domain.session.dto.ScenarioSnapshot;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "세션 시작 요청")
public record SessionStartRequest(
        @Schema(description = "학생 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long studentId,

        @Schema(description = "시나리오 ID", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        Long scenarioId,

        @Schema(description = "시나리오 스냅샷 정보", requiredMode = Schema.RequiredMode.REQUIRED)
        ScenarioSnapshot scenarioSnapshot
) {
}
