package com.ssafy.a202.domain.session.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.ssafy.a202.domain.session.dto.ScenarioSnapshot;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SessionStartRequest(
        Long studentId,
        Long scenarioId,
        ScenarioSnapshot scenarioSnapshot
) {
}
