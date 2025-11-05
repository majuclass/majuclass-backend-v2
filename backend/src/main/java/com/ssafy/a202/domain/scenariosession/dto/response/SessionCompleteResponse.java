package com.ssafy.a202.domain.scenariosession.dto.response;

import com.ssafy.a202.domain.scenariosession.entity.ScenarioSession;
import com.ssafy.a202.global.constants.SessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 세션 완료 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "세션 완료 응답")
public class SessionCompleteResponse {

    @Schema(description = "세션 ID", example = "1")
    private Long sessionId;

    @Schema(description = "학생 ID", example = "1")
    private Long studentId;

    @Schema(description = "시나리오 ID", example = "1")
    private Long scenarioId;

    @Schema(description = "세션 상태", example = "COMPLETED")
    private SessionStatus sessionStatus;

    @Schema(description = "세션 생성 시간", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "세션 완료 시간", example = "2025-01-15T10:45:00")
    private LocalDateTime completedAt;

    /**
     * ScenarioSession 엔티티를 SessionCompleteResponse로 변환
     */
    public static SessionCompleteResponse from(ScenarioSession session) {
        return SessionCompleteResponse.builder()
                .sessionId(session.getId())
                .studentId(session.getStudent().getId())
                .scenarioId(session.getScenario().getId())
                .sessionStatus(session.getSessionStatus())
                .createdAt(session.getCreatedAt())
                .completedAt(session.getUpdatedAt())
                .build();
    }
}