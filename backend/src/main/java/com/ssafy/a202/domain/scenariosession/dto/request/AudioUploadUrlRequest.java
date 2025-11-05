package com.ssafy.a202.domain.scenariosession.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 음성 업로드 URL 요청 DTO
 * 클라이언트는 세션 정보만 전달하고, 백엔드에서 S3 키를 자동 생성
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "음성 업로드 URL 요청")
public class AudioUploadUrlRequest {

    @NotNull(message = "세션 ID는 필수입니다")
    @Schema(description = "세션 ID", example = "1", required = true)
    private Long sessionId;

    @NotNull(message = "시퀀스 번호는 필수입니다")
    @Schema(description = "시퀀스 번호 (1부터 시작)", example = "1", required = true)
    private Integer sequenceNumber;

    @NotBlank(message = "컨텐츠 타입은 필수입니다")
    @Schema(description = "업로드하는 파일 타입",
            example = "audio/wav",
            required = true)
    private String contentType;
}