package com.ssafy.a202.domain.scenariosession.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 음성 답안 제출 응답 DTO (난이도 상)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "음성 답안 제출 응답")
public class AudioSubmitResponse {

    @Schema(description = "시나리오 ID", example = "1")
    private Long scenarioId;

    @Schema(description = "시퀀스 ID", example = "5")
    private Long sequenceId;

    @Schema(description = "시퀀스 번호", example = "1")
    private Integer sequenceNumber;

    @Schema(description = "제출한 음성 파일 S3 키", example = "students/1/sessions/5/seq_1_attempt_1.wav")
    private String audioS3Key;

    @Schema(description = "정답 여부", example = "true")
    private Boolean isCorrect;

    @Schema(description = "시도 번호 (1부터 시작)", example = "1")
    private Integer attemptNo;
}