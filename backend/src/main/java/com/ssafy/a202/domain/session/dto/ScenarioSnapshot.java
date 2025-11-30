package com.ssafy.a202.domain.session.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ScenarioSnapshot(
        String scnTitle,
        String scnDescription,
        String scnThumbnailS3Key,
        String scnBackgroundS3Key,
        String scnDifficultyLevel
) {
}
