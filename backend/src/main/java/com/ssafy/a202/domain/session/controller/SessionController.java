package com.ssafy.a202.domain.session.controller;

import com.ssafy.a202.common.entity.ApiResponse;
import com.ssafy.a202.common.entity.ApiResponseEntity;
import com.ssafy.a202.common.entity.SuccessCode;
import com.ssafy.a202.domain.session.dto.request.AnswerCreateRequest;
import com.ssafy.a202.domain.session.dto.request.SessionStartRequest;
import com.ssafy.a202.domain.session.dto.response.AnswerResponse;
import com.ssafy.a202.domain.session.dto.response.SessionResponse;
import com.ssafy.a202.domain.session.service.AnswerService;
import com.ssafy.a202.domain.session.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions")
@Tag(name = "Session", description = "시나리오 체험 세션 관리 API")
public class SessionController {

    private final SessionService sessionService;
    private final AnswerService answerService;

    @Operation(summary = "세션 시작", description = "학생이 시나리오 체험을 시작합니다.")
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<SessionResponse>> createSession(
            @RequestBody SessionStartRequest request
    ) {
        SessionResponse response = sessionService.create(request);
        return ApiResponseEntity.created(
                "/api/sessions/" + response.sessionId(),
                SuccessCode.SESSION_CREATE_SUCCESS,
                response
        );
    }

    @Operation(summary = "세션 완료", description = "진행 중인 세션을 정상 완료 처리합니다.")
    @PostMapping("/finish/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> finishSession(
            @Parameter(description = "완료할 세션 ID", required = true, example = "1")
            @PathVariable Long sessionId
    ) {
        sessionService.finish(sessionId);
        return ApiResponseEntity.success(
                SuccessCode.SESSION_FINISH_SUCCESS
        );
    }

    @Operation(summary = "세션 중단", description = "사용자가 세션을 명시적으로 중단합니다.")
    @PostMapping("/abort/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> abortSession(
            @Parameter(description = "중단할 세션 ID", required = true, example = "1")
            @PathVariable Long sessionId
    ) {
        sessionService.abort(sessionId);
        return ApiResponseEntity.success(
                SuccessCode.SESSION_ABORT_SUCCESS
        );
    }

    @Operation(summary = "답변 제출", description = "세션 내에서 학생의 답변을 저장합니다.")
    @PostMapping("/{sessionId}/answers")
    public ResponseEntity<ApiResponse<AnswerResponse>> createAnswer(
            @Parameter(description = "답변을 제출할 세션 ID", required = true, example = "1")
            @PathVariable Long sessionId,
            @RequestBody AnswerCreateRequest request
    ) {
        AnswerResponse response = answerService.create(sessionId, request);
        return ApiResponseEntity.created(
                "/api/sessions/"  + sessionId + "/answers/" + response.answerId(),
                SuccessCode.ANSWER_CREATE_SUCCESS,
                response
        );
    }

}
