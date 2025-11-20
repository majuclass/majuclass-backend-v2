package com.ssafy.a202.domain.student.dto.response;

import lombok.Builder;

/**
 * 학생별 세션 수 DTO
 */
@Builder
public record StudentSessionCountDto(
        Long studentId,
        String studentName,
        int sessionCount
) {
}