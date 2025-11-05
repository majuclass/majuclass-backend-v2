package com.ssafy.a202.domain.scenario.dto.response;

import com.ssafy.a202.domain.scenario.entity.SeqOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시퀀스 옵션 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시퀀스 선택 옵션")
public class OptionResponse {

    @Schema(description = "옵션 ID", example = "1")
    private Long optionId;

    @Schema(description = "옵션 번호 (1부터 시작)", example = "1")
    private int optionNumber;

    @Schema(description = "옵션 텍스트", example = "네, 아메리카노 한 잔 주세요")
    private String optionText;

    @Schema(description = "정답 여부", example = "true")
    private boolean isAnswer;

    /**
     * SeqOption 엔티티를 OptionResponse로 변환
     */
    public static OptionResponse from(SeqOption seqOption) {
        return OptionResponse.builder()
                .optionId(seqOption.getId())
                .optionNumber(seqOption.getOptionNo())
                .optionText(seqOption.getOptionText())
                .isAnswer(seqOption.isAnswer())
                .build();
    }
}