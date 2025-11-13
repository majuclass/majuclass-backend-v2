package com.ssafy.a202.domain.scenariosession.dto.response;

import com.ssafy.a202.domain.scenariosession.entity.SessionSttAnswer;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 오디오 답변 정보 DTO
 */
@Builder
public record AudioAnswerDto(
        Long answerId,
        Integer attemptNo,
        String audioUrl,
        Boolean isCorrect,
        LocalDateTime createdAt
) {
    /**
     * SessionSttAnswer 엔티티로부터 DTO 생성
     *
     * @param sessionSttAnswer 세션 STT 답변 엔티티
     * @param audioUrl 오디오 파일 S3 URL
     * @return AudioAnswerDto
     */
    public static AudioAnswerDto from(SessionSttAnswer sessionSttAnswer, String audioUrl) {
        return AudioAnswerDto.builder()
                .answerId(sessionSttAnswer.getId())
                .attemptNo(sessionSttAnswer.getAttemptNo())
                .audioUrl(audioUrl)
                .isCorrect(sessionSttAnswer.isCorrect())
                .createdAt(sessionSttAnswer.getCreatedAt())
                .build();
    }
}