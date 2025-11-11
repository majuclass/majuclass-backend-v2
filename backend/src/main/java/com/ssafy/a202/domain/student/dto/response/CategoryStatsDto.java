package com.ssafy.a202.domain.student.dto.response;

import lombok.Builder;

/**
 * 카테고리별 세션 통계 DTO
 */
@Builder
public record CategoryStatsDto(
        Long categoryId,
        String categoryName,
        int sessionCount,
        double percentage
) {
}