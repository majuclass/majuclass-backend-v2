package com.ssafy.a202.domain.scenario.dto.response;

import com.ssafy.a202.domain.scenario.entity.Scenario;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 시나리오 생성 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "시나리오 생성 응답")
public class ScenarioCreateResponse {

    @Schema(description = "시나리오 ID", example = "1")
    private Long scenarioId;

    @Schema(description = "시나리오 제목", example = "편의점에서 물건 사기")
    private String title;

    @Schema(description = "시나리오 설명", example = "편의점에서 간단한 물건을 구매하는 상황입니다")
    private String summary;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리 이름", example = "쇼핑")
    private String categoryName;

    @Schema(description = "썸네일 이미지 URL")
    private String thumbnailUrl;

    @Schema(description = "배경 이미지 URL")
    private String backgroundUrl;

    @Schema(description = "총 시퀀스 개수", example = "5")
    private int totalSequences;

    @Schema(description = "시퀀스 목록")
    private List<SequenceCreateResponse> sequences;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    /**
     * Entity에서 Response DTO로 변환
     */
    public static ScenarioCreateResponse from(Scenario scenario, String thumbnailUrl, String backgroundUrl) {
        return ScenarioCreateResponse.builder()
                .scenarioId(scenario.getId())
                .title(scenario.getTitle())
                .summary(scenario.getSummary())
                .categoryId(scenario.getScenarioCategory().getId())
                .categoryName(scenario.getScenarioCategory().getCategoryName())
                .thumbnailUrl(thumbnailUrl)
                .backgroundUrl(backgroundUrl)
                .totalSequences(scenario.getTotalSequences())
                .sequences(scenario.getScenarioSequences().stream()
                        .map(SequenceCreateResponse::from)
                        .toList())
                .createdAt(scenario.getCreatedAt())
                .build();
    }
}