package com.ssafy.a202.domain.session.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "답변 제출 저장 요청")
public record AnswerCreateRequest(
        @Schema(description = "시퀀스 ID", example = "10")
        Long sequenceId,

        @Schema(description = "시퀀스 번호", example = "1")
        int seqNo,

        @Schema(description = "시퀀스 질문 내용", example = "마트 점원에게 뭐라고 말해야 할까요?")
        String seqQuestion,

        @Schema(description = "정답 옵션 번호", example = "2")
        int correctOptionNo,

        @Schema(description = "정답 옵션 텍스트", example = "안녕하세요")
        String correctOptionText,

        @Schema(description = "정답 옵션 오디오 S3 키", example = "options/audio/correct123.mp3")
        String correctOptionS3Key,

        @Schema(description = "선택한 옵션 번호", example = "2")
        int selectedOptionNo,

        @Schema(description = "선택한 옵션 텍스트", example = "안녕하세요")
        String selectedOptionText,

        @Schema(description = "선택한 옵션 오디오 S3 키", example = "options/audio/selected123.mp3")
        String selectedOptionS3Key,

        @Schema(description = "정답 여부", example = "true")
        boolean isCorrect,

        @Schema(description = "n번째 시도", example = "1", minimum = "1")
        int attemptNo,

        @Schema(description = "녹음된 오디오 S3 키", example = "answers/audio/user456.mp3")
        String audioS3Key,

        @Schema(description = "음성 인식 텍스트", example = "안녕하세요")
        String transcribedText,

        @Schema(description = "유사도 점수", example = "0.95", minimum = "0.0", maximum = "1.0")
        double similarityScore
) {
}
