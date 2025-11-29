package com.ssafy.a202.domain.scenario.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.ssafy.a202.domain.scenario.entity.DifficultyLevel;
import com.ssafy.a202.domain.scenario.entity.Scenario;

import java.time.LocalDateTime;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ScenarioDetailResponse(
        Long scenarioId,
        String categoryName,
        String title,
        String description,
        DifficultyLevel difficultyLevel,
        String thumbnailUrl,
        String backgroundUrl,
        String createrFullName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SequenceResponse> sequences
) {
    public static ScenarioDetailResponse of(
            Scenario scenario,
            String thumbnailUrl,
            String backgroundUrl,
            List<SequenceResponse> sequences
    ) {
        return new ScenarioDetailResponse(
                scenario.getId(),
                scenario.getCategory().getName(),
                scenario.getTitle(),
                scenario.getDescription(),
                scenario.getDifficultyLevel(),
                thumbnailUrl,
                backgroundUrl,
                scenario.getUser().getFullName(),
                scenario.getCreatedAt(),
                scenario.getUpdatedAt(),
                sequences
        );
    }
}