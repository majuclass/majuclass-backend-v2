package com.ssafy.a202.domain.student.dto.response;

import lombok.Builder;

@Builder
public record AnswerAttemptDto(
        Integer attemptNo,
        Boolean isCorrect
) {
}