package com.ssafy.a202.domain.scenariosession.dto;

import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "시퀀스 응답 (옵션 제외)")
public class SequenceResponse {

    @Schema(description = "시퀀스 ID", example = "1")
    private Long sequenceId;

    @Schema(description = "시퀀스 번호 (1부터 시작)", example = "1")
    private int sequenceNumber;

    @Schema(description = "질문 내용", example = "주문 도와드릴까요?")
    private String question;

    @Schema(description = "시퀀스 미디어 파일의 PreSigned URL", example = "https://s3.amazonaws.com/bucket/sequence1.mp4?signature=...")
    private String mediaUrl;

    @Schema(description = "다음 시퀀스 존재 여부", example = "true")
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