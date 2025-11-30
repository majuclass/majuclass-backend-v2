package com.ssafy.a202.domain.category.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.ssafy.a202.domain.category.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "카테고리 응답")
public record CategoryResponse(
        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId,

        @Schema(description = "카테고리 이름", example = "사회생활")
        String name
) {
    public static CategoryResponse of(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }
}