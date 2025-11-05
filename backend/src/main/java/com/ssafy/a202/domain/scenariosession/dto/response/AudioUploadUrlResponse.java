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
            example = "https://ssafy-sai-project.s3.amazonaws.com/session_answers/test.jpg?X-Amz-Algorithm=...")
    private String url;

    @Schema(description = "S3에 요청한 경로",
            example = "session_answers/3/test.jpg")
    private String fileName;
}