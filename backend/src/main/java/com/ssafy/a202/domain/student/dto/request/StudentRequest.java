package com.ssafy.a202.domain.student.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "학생 등록/수정 요청")
public record StudentRequest(
        @Schema(description = "담임 선생님 ID (ADMIN, ORG_ADMIN만 입력 가능)", example = "1")
        Long userId,

        @Schema(description = "학생 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        String fullName
) {
}
