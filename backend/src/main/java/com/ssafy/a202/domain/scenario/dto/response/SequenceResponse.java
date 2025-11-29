package com.ssafy.a202.domain.scenario.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.ssafy.a202.domain.scenario.entity.Sequence;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SequenceResponse(
        Long sequenceId,
        Integer seqNo,
        String question,
        List<OptionResponse> options
) {
    public static SequenceResponse of(Sequence sequence, List<OptionResponse> options) {
        return new SequenceResponse(
                sequence.getId(),
                sequence.getSeqNo(),
                sequence.getQuestion(),
                options
        );
    }
}