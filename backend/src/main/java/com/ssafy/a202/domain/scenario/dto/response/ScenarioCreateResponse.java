package com.ssafy.a202.domain.scenario.dto.response;

import com.ssafy.a202.domain.scenario.entity.Scenario;

public record ScenarioCreateResponse(
        Long scenarioId
) {
    public static ScenarioCreateResponse of(Scenario scenario) {
        return new ScenarioCreateResponse(
                scenario.getId()
        );
    }
}
