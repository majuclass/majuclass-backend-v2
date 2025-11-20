package com.ssafy.a202.domain.scenario.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 옵션 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "옵션 수정 요청")
public class OptionUpdateRequest {

    @Schema(description = "옵션 ID (null인 경우 신규 생성)", example = "1")
    private Long id;

    @NotNull(message = "옵션 번호는 필수입니다")
    @Schema(description = "옵션 번호", example = "1", required = true)
    private Integer optionNo;

    @NotBlank(message = "옵션 텍스트는 필수입니다")
    @Schema(description = "옵션 텍스트", example = "빵", required = true)
    private String optionText;

    @NotBlank(message = "옵션 이미지 S3 키는 필수입니다")
    @Schema(description = "옵션 이미지 S3 키", example = "scenarios/options/uuid_image.jpg", required = true)
    private String optionS3Key;

    @NotNull(message = "정답 여부는 필수입니다")
    @Schema(description = "정답 여부", example = "true", required = true)
    private Boolean isAnswer;
}