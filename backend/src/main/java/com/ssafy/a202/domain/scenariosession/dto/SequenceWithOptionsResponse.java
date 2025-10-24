package com.ssafy.a202.domain.scenariosession.dto;

import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 시퀀스와 옵션들을 포함한 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SequenceWithOptionsResponse {

    private Long sequenceId;
    private int sequenceNumber;
    private String question;
    private String mediaUrl;
    private List<OptionResponse> options;

    /**
     * ScenarioSequence 엔티티를 SequenceWithOptionsResponse로 변환
     *
     * @param scenarioSequence 시퀀스 엔티티
     * @param mediaUrl 프리사인드 URL (Service에서 생성)
     */
    public static SequenceWithOptionsResponse from(ScenarioSequence scenarioSequence, String mediaUrl) {
        List<OptionResponse> options = scenarioSequence.getOptions().stream()
                .map(OptionResponse::from)
                .toList();

        return SequenceWithOptionsResponse.builder()
                .sequenceId(scenarioSequence.getId())
                .sequenceNumber(scenarioSequence.getSeqNo())
                .question(scenarioSequence.getQuestion())
                .mediaUrl(mediaUrl)
                .options(options)
                .build();
    }
}