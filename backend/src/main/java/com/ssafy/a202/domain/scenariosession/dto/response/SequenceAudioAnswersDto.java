package com.ssafy.a202.domain.scenariosession.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * 시퀀스별 오디오 답변 DTO
 */
@Builder
public record SequenceAudioAnswersDto(
        Long sequenceId,
        Integer sequenceNumber,
        Integer totalAttempts,
        List<AudioAnswerDto> audioAnswers
) {
    /**
     * 응답 생성
     *
     * @param sequenceId 시퀀스 ID
     * @param sequenceNumber 시퀀스 번호
     * @param audioAnswers 오디오 답변 목록
     * @return SequenceAudioAnswersDto
     */
    public static SequenceAudioAnswersDto of(Long sequenceId, Integer sequenceNumber, List<AudioAnswerDto> audioAnswers) {
        return SequenceAudioAnswersDto.builder()
                .sequenceId(sequenceId)
                .sequenceNumber(sequenceNumber)
                .totalAttempts(audioAnswers.size())
                .audioAnswers(audioAnswers)
                .build();
    }
}