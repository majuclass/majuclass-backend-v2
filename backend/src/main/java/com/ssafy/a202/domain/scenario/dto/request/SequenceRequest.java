package com.ssafy.a202.domain.scenario.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SequenceRequest(
        int seqNo,
        String question,
        List<OptionRequest> options
) {}
