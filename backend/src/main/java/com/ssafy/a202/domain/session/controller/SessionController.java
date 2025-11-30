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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final AnswerService answerService;

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

    @PostMapping("/finish/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> finishSession(
            @PathVariable Long sessionId
    ) {
        sessionService.finish(sessionId);
        return ApiResponseEntity.success(
                SuccessCode.SESSION_FINISH_SUCCESS
        );
    }

    @PostMapping("/abort/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> abortSession(
            @PathVariable Long sessionId
    ) {
        sessionService.abort(sessionId);
        return ApiResponseEntity.success(
                SuccessCode.SESSION_ABORT_SUCCESS
        );
    }

    @PostMapping("/{sessionId}/answers")
    public ResponseEntity<ApiResponse<AnswerResponse>> createAnswer(
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
