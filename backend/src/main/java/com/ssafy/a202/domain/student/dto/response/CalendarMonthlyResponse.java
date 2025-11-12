package com.ssafy.a202.domain.student.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * 월별 달력 응답 DTO
 */
@Builder
public record CalendarMonthlyResponse(
        int year,
        int month,
        List<CalendarDayStatsDto> dailyStats,
        int totalDays
) {
}