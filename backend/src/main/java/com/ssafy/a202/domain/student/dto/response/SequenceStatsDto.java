package com.ssafy.a202.domain.student.dto.response;

import lombok.Builder;

/**
 * 시퀀스별 통계 DTO
 */
@Builder
public record SequenceStatsDto(
        Long sequenceId,
        Integer sequenceNumber,
        String question,
        Integer successAttempt,  // 성공한 시도 번호 (1번째, 2번째...), 실패 시 null
        Double accuracyRate,     // 정답률 (100 / successAttempt)
        Boolean isCorrect        // 최종적으로 정답 여부
) {
}