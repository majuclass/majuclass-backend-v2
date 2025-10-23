package com.ssafy.a202.domain.scenariosession.dto;

import com.ssafy.a202.domain.scenario.entity.SeqOption;
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
public class OptionResponse {

    private Long optionId;
    private int optionNumber;
    private String optionText;
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