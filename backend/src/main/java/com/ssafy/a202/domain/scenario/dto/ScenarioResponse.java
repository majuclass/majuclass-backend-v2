package com.ssafy.a202.domain.scenario.dto;

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
    private String thumbnailS3Bucket;
    private String thumbnailS3Key;
    private Long categoryId;
    private String categoryName;
    private int totalSequences;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Scenario 엔티티를 ScenarioResponse로 변환
     */
    public static ScenarioResponse from(Scenario scenario) {
        ScenarioCategory category = scenario.getScenarioCategory();

        return ScenarioResponse.builder()
                .id(scenario.getId())
                .title(scenario.getTitle())
                .summary(scenario.getSummary())
                .thumbnailS3Bucket(scenario.getThumbnailS3Bucket())
                .thumbnailS3Key(scenario.getThumbnailS3Key())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getCategoryName() : null)
                .totalSequences(scenario.getTotalSequences())
                .createdAt(scenario.getCreatedAt())
                .updatedAt(scenario.getUpdatedAt())
                .build();
    }
}