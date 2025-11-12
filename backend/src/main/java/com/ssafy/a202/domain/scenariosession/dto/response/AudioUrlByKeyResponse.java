package com.ssafy.a202.domain.scenariosession.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * S3 Key로 생성된 오디오 URL 응답 DTO
 */
@Builder
@Schema(description = "오디오 조회용 Pre-signed URL 응답")
public record AudioUrlByKeyResponse(
        @Schema(description = "S3 파일 키", example = "students/123/sessions/456/seq_1_attempt_1.wav")
        String s3Key,

        @Schema(description = "오디오 파일 조회용 Pre-signed URL",
                example = "https://s3.amazonaws.com/bucket/students/123/sessions/456/seq_1_attempt_1.wav?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-...")
        String audioUrl
) {
    /**
     * 응답 생성
     *
     * @param s3Key S3 객체 키
     * @param audioUrl 생성된 Pre-signed URL
     * @return AudioUrlByKeyResponse
     */
    public static AudioUrlByKeyResponse of(String s3Key, String audioUrl) {
        return AudioUrlByKeyResponse.builder()
                .s3Key(s3Key)
                .audioUrl(audioUrl)
                .build();
    }
}