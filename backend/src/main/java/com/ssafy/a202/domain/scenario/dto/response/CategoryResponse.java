package com.ssafy.a202.domain.scenario.dto.response;

import com.ssafy.a202.domain.scenario.entity.ScenarioCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 카테고리 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "카테고리 응답")
public class CategoryResponse {

    @Schema(description = "카테고리 ID", example = "1")
    private Long id;

    @Schema(description = "카테고리 이름", example = "일상생활")
    private String categoryName;

    /**
     * 엔티티로부터 DTO 생성
     */
    public static CategoryResponse from(ScenarioCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .build();
    }
}