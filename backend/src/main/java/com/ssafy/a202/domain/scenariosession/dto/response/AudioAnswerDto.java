package com.ssafy.a202.domain.scenariosession.dto.response;

import com.ssafy.a202.domain.scenariosession.entity.SessionAnswer;
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
     * SessionAnswer 엔티티로부터 DTO 생성
     *
     * @param sessionAnswer 세션 답변 엔티티
     * @param audioUrl 오디오 파일 S3 URL
     * @return AudioAnswerDto
     */
    public static AudioAnswerDto from(SessionAnswer sessionAnswer, String audioUrl) {
        return AudioAnswerDto.builder()
                .answerId(sessionAnswer.getId())
                .attemptNo(sessionAnswer.getAttemptNo())
                .audioUrl(audioUrl)
                .isCorrect(sessionAnswer.getIsCorrect())
                .createdAt(sessionAnswer.getCreatedAt())
                .build();
    }
}