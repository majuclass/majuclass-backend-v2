package com.ssafy.a202.domain.scenario.dto;

import com.ssafy.a202.global.constants.Difficulty;
import com.ssafy.a202.domain.scenario.entity.Scenario;
import com.ssafy.a202.domain.scenario.entity.ScenarioCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 시나리오 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioResponse {

    private Long id;
    private String title;
    private String summary;
    private String thumbnailUrl;
    private Long categoryId;
    private String categoryName;
    private int totalSequences;
    private Difficulty difficulty;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Scenario 엔티티를 ScenarioResponse로 변환
     */
    public static ScenarioResponse from(Scenario scenario, String thumbnailUrl) {
        ScenarioCategory category = scenario.getScenarioCategory();

        return ScenarioResponse.builder()
                .id(scenario.getId())
                .title(scenario.getTitle())
                .summary(scenario.getSummary())
                .thumbnailUrl(thumbnailUrl)
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getCategoryName() : null)
                .totalSequences(scenario.getTotalSequences())
                .difficulty(scenario.getDifficulty())
                .createdAt(scenario.getCreatedAt())
                .updatedAt(scenario.getUpdatedAt())
                .build();
    }
}