package com.ssafy.a202.domain.scenario.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.ssafy.a202.domain.scenario.entity.DifficultyLevel;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ScenarioRequest(
        Long categoryId,
        String title,
        String description,
        String thumbnailS3Key,
        String backgroundS3Key,
        DifficultyLevel difficultyLevel,
        List<SequenceRequest> sequences
) {}
