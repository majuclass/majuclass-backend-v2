package com.ssafy.a202.domain.scenariosession.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * 오디오 답변 목록 조회 응답 DTO
 */
@Builder
public record AudioAnswerListResponse(
        Long sessionId,
        Long sequenceId,
        Integer sequenceNumber,
        Integer totalAttempts,
        List<AudioAnswerDto> audioAnswers
) {
    /**
     * 응답 생성
     *
     * @param sessionId 세션 ID
     * @param sequenceId 시퀀스 ID
     * @param sequenceNumber 시퀀스 번호
     * @param audioAnswers 오디오 답변 목록
     * @return AudioAnswerListResponse
     */
    public static AudioAnswerListResponse of(Long sessionId, Long sequenceId, Integer sequenceNumber, List<AudioAnswerDto> audioAnswers) {
        return AudioAnswerListResponse.builder()
                .sessionId(sessionId)
                .sequenceId(sequenceId)
                .sequenceNumber(sequenceNumber)
                .totalAttempts(audioAnswers.size())
                .audioAnswers(audioAnswers)
                .build();
    }
}