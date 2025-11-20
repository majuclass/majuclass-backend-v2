package com.ssafy.a202.domain.scenariosession.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * 오디오 답변 목록 조회 응답 DTO (세션의 모든 시퀀스별 오디오 답변)
 */
@Builder
public record AudioAnswerListResponse(
        Long sessionId,
        List<SequenceAudioAnswersDto> sequences
) {
    /**
     * 응답 생성
     *
     * @param sessionId 세션 ID
     * @param sequences 시퀀스별 오디오 답변 목록
     * @return AudioAnswerListResponse
     */
    public static AudioAnswerListResponse of(Long sessionId, List<SequenceAudioAnswersDto> sequences) {
        return AudioAnswerListResponse.builder()
                .sessionId(sessionId)
                .sequences(sequences)
                .build();
    }
}