package com.ssafy.a202.domain.scenario.dto.request;

import com.ssafy.a202.global.constants.ImageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이미지 업로드용 Presigned URL 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이미지 업로드용 Presigned URL 요청")
public class ImageUploadUrlRequest {

    @NotNull(message = "이미지 타입은 필수입니다")
    @Schema(description = "이미지 타입 (THUMBNAIL: 썸네일, BACKGROUND: 배경, OPTION: 옵션)",
            example = "THUMBNAIL",
            required = true)
    private ImageType imageType;

    @NotBlank(message = "Content-Type은 필수입니다")
    @Schema(description = "파일 Content-Type", example = "image/jpeg", required = true)
    private String contentType;
}