package com.ssafy.a202.domain.scenariosession.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * S3 Key로 오디오 URL 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@Schema(description = "S3 Key로 오디오 조회용 URL 생성 요청 (FastAPI STT 분석용)")
public class AudioUrlByKeyRequest {

    @NotBlank(message = "S3 키는 필수입니다")
    @Schema(description = "S3에 업로드된 오디오 파일 키",
            example = "students/123/sessions/456/seq_1_attempt_1.wav",
            required = true)
    private String s3Key;
}