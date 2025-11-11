package com.ssafy.a202.domain.student.dto.response;

import com.ssafy.a202.global.constants.SessionStatus;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 세션 목록 아이템 DTO
 */
@Builder
public record SessionListItemDto(
        Long sessionId,
        Long scenarioId,
        String scenarioTitle,
        String thumbnailUrl,
        Long categoryId,
        String categoryName,
        SessionStatus status,
        LocalDateTime createdAt
) {
}