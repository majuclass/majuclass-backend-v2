package com.ssafy.a202.domain.scenariosession.dto;

import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시퀀스 응답 DTO (옵션 제외)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SequenceResponse {

    private Long sequenceId;
    private int sequenceNumber;
    private String question;
    private String mediaUrl;
    private boolean hasNext;

    /**
     * ScenarioSequence 엔티티를 SequenceResponse로 변환
     *
     * @param scenarioSequence 시퀀스 엔티티
     * @param mediaUrl 프리사인드 URL (Service에서 생성)
     * @param hasNext 다음 시퀀스 존재 여부
     */
    public static SequenceResponse from(ScenarioSequence scenarioSequence, String mediaUrl, boolean hasNext) {
        return SequenceResponse.builder()
                .sequenceId(scenarioSequence.getId())
                .sequenceNumber(scenarioSequence.getSeqNo())
                .question(scenarioSequence.getQuestion())
                .mediaUrl(mediaUrl)
                .hasNext(hasNext)
                .build();
    }
}