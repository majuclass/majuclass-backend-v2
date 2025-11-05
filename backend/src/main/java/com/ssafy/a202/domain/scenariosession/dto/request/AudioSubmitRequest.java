package com.ssafy.a202.domain.scenariosession.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 음성 답안 제출 요청 DTO (난이도 상)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "음성 답안 제출 요청")
public class AudioSubmitRequest {

    @NotNull(message = "세션 ID는 필수입니다")
    @Schema(description = "세션 ID", example = "1", required = true)
    private Long sessionId;

    @NotNull(message = "시나리오 ID는 필수입니다")
    @Schema(description = "시나리오 ID", example = "1", required = true)
    private Long scenarioId;

    @NotNull(message = "시퀀스 번호는 필수입니다")
    @Schema(description = "시퀀스 번호 (1부터 시작)", example = "1", required = true)
    private Integer sequenceNumber;

    @NotBlank(message = "S3 파일 키는 필수입니다")
    @Schema(description = "S3에 업로드된 음성 파일 키",
            example = "students/1/sessions/5/seq_1_attempt_1.wav",
            required = true)
    private String audioS3Key;
}