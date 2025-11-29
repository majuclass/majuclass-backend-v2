package com.ssafy.a202.domain.scenario.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.ssafy.a202.domain.scenario.entity.Option;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OptionResponse(
        Long optionId,
        Integer optionNo,
        Boolean isCorrect,
        String optionText,
        String optionUrl
) {
    public static OptionResponse of(Option option, String optionUrl) {
        return new OptionResponse(
                option.getId(),
                option.getOptionNo(),
                option.isCorrect(),
                option.getOptionText(),
                optionUrl
        );
    }
}