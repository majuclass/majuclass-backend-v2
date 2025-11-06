package com.ssafy.a202.domain.scenario.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이미지 업로드용 Presigned URL 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이미지 업로드용 Presigned URL 응답")
public class ImageUploadUrlResponse {

    @Schema(description = "업로드용 Presigned URL", example = "https://...")
    private String presignedUrl;

    @Schema(description = "S3 키 (시나리오 생성 시 전달해야 함)", example = "scenarios/options/uuid_image.jpg")
    private String s3Key;

    public static ImageUploadUrlResponse of(String presignedUrl, String s3Key) {
        return ImageUploadUrlResponse.builder()
                .presignedUrl(presignedUrl)
                .s3Key(s3Key)
                .build();
    }
}