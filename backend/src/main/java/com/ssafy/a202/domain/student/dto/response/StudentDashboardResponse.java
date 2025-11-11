package com.ssafy.a202.domain.student.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * 학생 대시보드 응답 DTO
 */
@Builder
public record StudentDashboardResponse(
        List<CategoryStatsDto> categoryStats,
        List<SessionListItemDto> sessions,
        int totalSessions
) {
}