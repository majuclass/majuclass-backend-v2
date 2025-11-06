package com.ssafy.a202.domain.scenario.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 시퀀스(질문) 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시퀀스 생성 요청")
public class SequenceCreateRequest {

    @NotNull(message = "시퀀스 번호는 필수입니다")
    @Min(value = 1, message = "시퀀스 번호는 1 이상이어야 합니다")
    @Schema(description = "시퀀스 번호 (1부터 시작)", example = "1", required = true)
    private Integer seqNo;

    @NotBlank(message = "질문은 필수입니다")
    @Schema(description = "질문 내용", example = "점원에게 어떻게 인사하시겠습니까?", required = true)
    private String question;

    @NotNull(message = "옵션 정보는 필수입니다")
    @Size(min = 2, message = "최소 2개 이상의 옵션이 필요합니다")
    @Valid
    @Schema(description = "옵션 목록", required = true)
    private List<OptionCreateRequest> options;
}