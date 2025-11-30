package com.ssafy.a202.domain.scenario.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.ssafy.a202.domain.scenario.entity.DifficultyLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "시나리오 생성/수정 요청")
public record ScenarioRequest(
        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId,

        @Schema(description = "시나리오 제목", example = "마트에서 인사하기", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Schema(description = "시나리오 설명", example = "마트에서 점원에게 인사하는 연습")
        String description,

        @Schema(description = "썸네일 이미지 S3 키", example = "scenarios/thumbnails/abc123.jpg")
        String thumbnailS3Key,

        @Schema(description = "배경 이미지 S3 키", example = "scenarios/backgrounds/def456.jpg")
        String backgroundS3Key,

        @Schema(description = "난이도 레벨", example = "EASY", requiredMode = Schema.RequiredMode.REQUIRED)
        DifficultyLevel difficultyLevel,

        @Schema(description = "시퀀스 목록", requiredMode = Schema.RequiredMode.REQUIRED)
        List<SequenceRequest> sequences
) {}
