package com.ssafy.a202.domain.scenario.dto.response;

import com.ssafy.a202.domain.scenario.entity.Scenario;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 시나리오 수정 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시나리오 수정 응답")
public class ScenarioUpdateResponse {

    @Schema(description = "시나리오 ID", example = "1")
    private Long scenarioId;

    @Schema(description = "시나리오 제목", example = "편의점에서 물건 사기")
    private String title;

    @Schema(description = "시나리오 설명", example = "편의점에서 간단한 물건을 구매하는 상황입니다")
    private String summary;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "썸네일 URL", example = "https://s3.amazonaws.com/...")
    private String thumbnailUrl;

    @Schema(description = "배경 이미지 URL", example = "https://s3.amazonaws.com/...")
    private String backgroundUrl;

    @Schema(description = "총 시퀀스 개수", example = "5")
    private int totalSequences;

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    public static ScenarioUpdateResponse from(Scenario scenario, String thumbnailUrl, String backgroundUrl) {
        return ScenarioUpdateResponse.builder()
                .scenarioId(scenario.getId())
                .title(scenario.getTitle())
                .summary(scenario.getSummary())
                .categoryId(scenario.getScenarioCategory().getId())
                .thumbnailUrl(thumbnailUrl)
                .backgroundUrl(backgroundUrl)
                .totalSequences(scenario.getTotalSequences())
                .updatedAt(scenario.getUpdatedAt())
                .build();
    }
}