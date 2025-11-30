package com.ssafy.a202.domain.student.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record StudentRequest(
        Long userId, // 담임 선생을 직접 지정하고 싶다면 입력받음.
        String fullName
) {
}
