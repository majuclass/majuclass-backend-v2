package com.ssafy.a202.domain.student.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * 학생 세션 목록 응답 DTO
 */
@Builder
public record StudentSessionsResponse(
        List<SessionListItemDto> sessions,
        int totalCount
) {
}