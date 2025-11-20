package com.ssafy.a202.domain.scenario.dto.response;

import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "시퀀스와 옵션 응답")
public class SequenceWithOptionsResponse {

    @Schema(description = "시퀀스 ID", example = "1")
    private Long sequenceId;

    @Schema(description = "시퀀스 번호 (1부터 시작)", example = "1")
    private int sequenceNumber;

    @Schema(description = "질문 내용", example = "주문 도와드릴까요?")
    private String question;

    @Schema(description = "선택 옵션 목록")
    private List<OptionResponse> options;

    /**
     * ScenarioSequence 엔티티를 SequenceWithOptionsResponse로 변환
     *
     * @param scenarioSequence 시퀀스 엔티티
     */
    public static SequenceWithOptionsResponse from(ScenarioSequence scenarioSequence) {
        List<OptionResponse> options = scenarioSequence.getOptions().stream()
                .map(OptionResponse::from)
                .toList();

        return SequenceWithOptionsResponse.builder()
                .sequenceId(scenarioSequence.getId())
                .sequenceNumber(scenarioSequence.getSeqNo())
                .question(scenarioSequence.getQuestion())
                .options(options)
                .build();
    }
}