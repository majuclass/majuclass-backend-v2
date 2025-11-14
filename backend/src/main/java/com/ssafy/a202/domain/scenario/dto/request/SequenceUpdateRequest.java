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
 * 시퀀스 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시퀀스 수정 요청")
public class SequenceUpdateRequest {

    @Schema(description = "시퀀스 ID (null인 경우 신규 생성)", example = "1")
    private Long id;

    @NotNull(message = "시퀀스 번호는 필수입니다")
    @Schema(description = "시퀀스 번호 (1부터 시작)", example = "1", required = true)
    private Integer seqNo;

    @NotBlank(message = "질문은 필수입니다")
    @Schema(description = "질문", example = "편의점에서 빵을 사려면 뭐라고 말해야 할까요?", required = true)
    private String question;

    @NotNull(message = "옵션은 필수입니다")
    @Size(min = 1, message = "최소 1개 이상의 옵션이 필요합니다")
    @Valid
    @Schema(description = "옵션 목록", required = true)
    private List<OptionUpdateRequest> options;
}