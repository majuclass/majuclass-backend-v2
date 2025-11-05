package com.ssafy.a202.domain.scenario.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssafy.a202.domain.scenario.entity.Scenario;
import com.ssafy.a202.domain.scenario.entity.ScenarioCategory;
import io.swagger.v3.oas.annotations.media.Schema;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "시나리오 응답")
public class ScenarioResponse {

    @Schema(description = "시나리오 ID", example = "1")
    private Long id;

    @Schema(description = "시나리오 제목", example = "카페 시나리오")
    private String title;

    @Schema(description = "시나리오 요약", example = "카페에서 주문하는 상황을 연습합니다")
    private String summary;

    @Schema(description = "썸네일 이미지 URL", example = "https://s3.amazonaws.com/bucket/thumbnail1.jpg")
    private String thumbnailUrl;

    @Schema(description = "백그라운드 이미지 URL", example = "https://s3.amazonaws.com/bucket/background1.jpg")
    private String backgroundUrl;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리 이름", example = "주문하기")
    private String categoryName;

    @Schema(description = "총 시퀀스 개수", example = "5")
    private int totalSequences;

    @Schema(description = "생성 일시", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-01-15T10:30:00")
    private LocalDateTime updatedAt;

    /**
     * Scenario 엔티티를 ScenarioResponse로 변환 (목록 조회용 - 썸네일만)
     */
    public static ScenarioResponse fromList(Scenario scenario, String thumbnailUrl) {
        ScenarioCategory category = scenario.getScenarioCategory();

        return ScenarioResponse.builder()
                .id(scenario.getId())
                .title(scenario.getTitle())
                .summary(scenario.getSummary())
                .thumbnailUrl(thumbnailUrl)
                .backgroundUrl(null)  // 목록 조회에서는 null
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getCategoryName() : null)
                .totalSequences(scenario.getTotalSequences())
                .createdAt(scenario.getCreatedAt())
                .updatedAt(scenario.getUpdatedAt())
                .build();
    }

    /**
     * Scenario 엔티티를 ScenarioResponse로 변환 (상세 조회용 - 백그라운드만)
     */
    public static ScenarioResponse fromDetail(Scenario scenario, String backgroundUrl) {
        ScenarioCategory category = scenario.getScenarioCategory();

        return ScenarioResponse.builder()
                .id(scenario.getId())
                .title(scenario.getTitle())
                .summary(scenario.getSummary())
                .thumbnailUrl(null)  // 상세 조회에서는 null
                .backgroundUrl(backgroundUrl)
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getCategoryName() : null)
                .totalSequences(scenario.getTotalSequences())
                .createdAt(scenario.getCreatedAt())
                .updatedAt(scenario.getUpdatedAt())
                .build();
    }
}