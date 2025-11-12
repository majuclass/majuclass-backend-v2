package com.ssafy.a202.domain.student.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

/**
 * 달력의 특정 날짜 통계 DTO
 */
@Builder
public record CalendarDayStatsDto(
        LocalDate date,
        List<StudentSessionCountDto> studentSessions,
        int totalSessionCount
) {
}