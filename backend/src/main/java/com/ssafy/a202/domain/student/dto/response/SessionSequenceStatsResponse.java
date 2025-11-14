package com.ssafy.a202.domain.student.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * 세션의 시퀀스별 통계 응답 DTO
 */
@Builder
public record SessionSequenceStatsResponse(
        Long sessionId,
        String scenarioTitle,
        List<SequenceStatsDto> sequenceStats,
        Integer totalSequences,
        Integer completedSequences,
        Double averageAccuracy
) {
}