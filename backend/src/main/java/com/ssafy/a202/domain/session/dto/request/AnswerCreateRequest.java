package com.ssafy.a202.domain.session.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnswerCreateRequest(
        Long sequenceId,
        int seqNo,
        String seqQuestion,

        int correctOptionNo,
        String correctOptionText,
        String correctOptionS3Key,

        int selectedOptionNo,
        String selectedOptionText,
        String selectedOptionS3Key,

        boolean isCorrect,
        int attemptNo,

        String audioS3Key,
        String transcribedText,
        double similarityScore
) {
}
