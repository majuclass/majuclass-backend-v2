package com.ssafy.a202.domain.student.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * 학생 대시보드 통계 응답 DTO
 */
@Builder
public record StudentDashboardStatsResponse(
        List<CategoryStatsDto> categoryStats,
        int totalSessions
) {
}