package com.ssafy.a202.domain.student.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

/**
 * 특정 날짜의 세션 목록 응답 DTO
 */
@Builder
public record DailySessionListResponse(
        LocalDate date,
        Long studentId,
        String studentName,
        List<SessionListItemDto> sessions,
        int totalCount
) {
}