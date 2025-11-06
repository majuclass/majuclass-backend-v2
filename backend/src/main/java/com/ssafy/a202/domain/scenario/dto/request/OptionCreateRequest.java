package com.ssafy.a202.domain.scenario.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 옵션 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "옵션 생성 요청")
public class OptionCreateRequest {

    @NotNull(message = "옵션 번호는 필수입니다")
    @Min(value = 1, message = "옵션 번호는 1 이상이어야 합니다")
    @Schema(description = "옵션 번호 (1부터 시작)", example = "1", required = true)
    private Integer optionNo;

    @NotBlank(message = "옵션 텍스트는 필수입니다")
    @Schema(description = "옵션 텍스트", example = "안녕하세요", required = true)
    private String optionText;

    @NotBlank(message = "옵션 이미지 S3 키는 필수입니다")
    @Schema(description = "옵션 이미지 S3 키", example = "scenarios/options/uuid_image.jpg", required = true)
    private String optionS3Key;

    @NotNull(message = "정답 여부는 필수입니다")
    @Schema(description = "정답 여부", example = "true", required = true)
    private Boolean isAnswer;
}