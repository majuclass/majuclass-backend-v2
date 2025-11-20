package com.ssafy.a202.domain.scenario.dto.response;

import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 시퀀스 생성 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "시퀀스 생성 응답")
public class SequenceCreateResponse {

    @Schema(description = "시퀀스 ID", example = "1")
    private Long sequenceId;

    @Schema(description = "시퀀스 번호", example = "1")
    private int sequenceNumber;

    @Schema(description = "질문 내용", example = "점원에게 어떻게 인사하시겠습니까?")
    private String question;

    @Schema(description = "옵션 목록")
    private List<OptionCreateResponse> options;

    /**
     * Entity에서 Response DTO로 변환
     */
    public static SequenceCreateResponse from(ScenarioSequence sequence) {
        return SequenceCreateResponse.builder()
                .sequenceId(sequence.getId())
                .sequenceNumber(sequence.getSeqNo())
                .question(sequence.getQuestion())
                .options(sequence.getOptions().stream()
                        .map(OptionCreateResponse::from)
                        .toList())
                .build();
    }
}