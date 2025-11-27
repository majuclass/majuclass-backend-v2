package com.ssafy.a202.domain.scenario.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OptionRequest(
        int optionNo,
        boolean isCorrect,
        String optionText,
        String optionS3Key
) {
}
