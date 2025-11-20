package com.ssafy.a202.domain.scenariosession.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 음성 업로드 URL 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "음성 업로드 URL 응답")
public class AudioUploadUrlResponse {

    @Schema(description = "업로드용 Presigned URL",
            example = "https://ssafy-sai-project.s3.amazonaws.com/students/1/sessions/1/seq_1_attempt_1.wav?X-Amz-Algorithm=...")
    private String presignedUrl;

    @Schema(description = "S3 키 (음성 답안 제출 시 전달해야 함)",
            example = "students/1/sessions/1/seq_1_attempt_1.wav")
    private String s3Key;

    public static AudioUploadUrlResponse of(String presignedUrl, String s3Key) {
        return AudioUploadUrlResponse.builder()
                .presignedUrl(presignedUrl)
                .s3Key(s3Key)
                .build();
    }
}