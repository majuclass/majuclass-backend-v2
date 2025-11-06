package com.ssafy.a202.domain.scenario.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 시나리오 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시나리오 생성 요청")
public class ScenarioCreateRequest {

    @NotBlank(message = "시나리오 제목은 필수입니다")
    @Size(max = 100, message = "시나리오 제목은 100자 이하여야 합니다")
    @Schema(description = "시나리오 제목", example = "편의점에서 물건 사기", required = true)
    private String title;

    @NotBlank(message = "시나리오 설명은 필수입니다")
    @Schema(description = "시나리오 설명", example = "편의점에서 간단한 물건을 구매하는 상황입니다", required = true)
    private String summary;

    @NotNull(message = "카테고리 ID는 필수입니다")
    @Schema(description = "카테고리 ID", example = "1", required = true)
    private Long categoryId;

    @Schema(description = "썸네일 S3 키 (선택)", example = "scenarios/thumbnails/uuid_image.jpg")
    private String thumbnailS3Key;

    @Schema(description = "배경 이미지 S3 키 (선택)", example = "scenarios/backgrounds/uuid_image.jpg")
    private String backgroundS3Key;

    @NotNull(message = "시퀀스 정보는 필수입니다")
    @Size(min = 1, message = "최소 1개 이상의 질문이 필요합니다")
    @Valid
    @Schema(description = "시퀀스(질문) 목록", required = true)
    private List<SequenceCreateRequest> sequences;
}