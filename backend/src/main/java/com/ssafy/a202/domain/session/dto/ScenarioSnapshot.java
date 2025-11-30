package com.ssafy.a202.domain.session.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "시나리오 스냅샷")
public record ScenarioSnapshot(
        @Schema(description = "시나리오 제목", example = "마트에서 인사하기")
        String scnTitle,

        @Schema(description = "시나리오 설명", example = "마트에서 점원에게 인사하는 연습")
        String scnDescription,

        @Schema(description = "썸네일 S3 키", example = "scenarios/thumbnails/abc123.jpg")
        String scnThumbnailS3Key,

        @Schema(description = "배경 이미지 S3 키", example = "scenarios/backgrounds/def456.jpg")
        String scnBackgroundS3Key,

        @Schema(description = "난이도 레벨", example = "EASY", allowableValues = {"EASY", "MEDIUM", "HARD"})
        String scnDifficultyLevel
) {
}
