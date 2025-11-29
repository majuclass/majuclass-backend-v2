package com.ssafy.a202.domain.scenario.dto.response;

import com.ssafy.a202.domain.scenario.entity.DifficultyLevel;
import com.ssafy.a202.domain.scenario.entity.Scenario;

import java.time.LocalDateTime;

public record ScenarioPreviewResponse(
        Long scenarioId,
        String categoryName,
        String title,
        String description,
        DifficultyLevel difficultyLevel,
        String thumbnailUrl,
        String backgroundUrl,
        String createrFullName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ScenarioPreviewResponse of(Scenario scenario, String thumbnailUrl, String backgroundUrl) {
        return new ScenarioPreviewResponse(
                scenario.getId(),
                scenario.getCategory().getName(),
                scenario.getTitle(),
                scenario.getDescription(),
                scenario.getDifficultyLevel(),
                thumbnailUrl,
                backgroundUrl,
                scenario.getUser().getFullName(),
                scenario.getCreatedAt(),
                scenario.getUpdatedAt()
        );
    }
}
