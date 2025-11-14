package com.ssafy.a202.domain.scenariosession.controller;

import com.ssafy.a202.domain.scenariosession.dto.request.*;
import com.ssafy.a202.domain.scenariosession.dto.response.*;
import com.ssafy.a202.domain.scenariosession.service.ScenarioSessionService;
import com.ssafy.a202.global.constants.SuccessCode;
import com.ssafy.a202.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 시나리오 세션 컨트롤러
 */
@RestController
@RequestMapping("/scenario-sessions")
@RequiredArgsConstructor
@Tag(name = "ScenarioSession", description = "시나리오 세션 관련 API")
public class ScenarioSessionController {

    private final ScenarioSessionService scenarioSessionService;

    @Operation(summary = "세션 시작", description = "학생 ID와 시나리오 ID를 받아 새로운 세션을 시작합니다.")
    @PostMapping("/start")
    public ApiResponse<SessionStartResponse> startSession(
            @Valid @RequestBody SessionStartRequest request) {
        SessionStartResponse response = scenarioSessionService.startSession(request);
        return ApiResponse.success(SuccessCode.SESSION_START_SUCCESS, response);
    }

    @Operation(summary = "음성 업로드 URL 생성", description = "음성 파일 업로드를 위한 Presigned URL을 생성합니다.")
    @PostMapping("/audio-upload-url")
    public ApiResponse<AudioUploadUrlResponse> generateAudioUploadUrl(
            @Valid @RequestBody AudioUploadUrlRequest request) {
        AudioUploadUrlResponse response = scenarioSessionService.generateAudioUploadUrl(request);
        return ApiResponse.success(SuccessCode.AUDIO_UPLOAD_URL_GENERATED, response);
    }

    @Operation(summary = "답안 제출 및 검증 (난이도 하/중)", description = "사용자가 선택한 답안을 제출하고 정답 여부를 확인합니다.")
    @PostMapping("/submit-answer")
    public ApiResponse<AnswerCheckResponse> submitAnswer(
            @Valid @RequestBody AnswerSubmitRequest request) {
        AnswerCheckResponse response = scenarioSessionService.submitAnswer(request);
        return ApiResponse.success(SuccessCode.ANSWER_CHECK_SUCCESS, response);
    }

    /*
    @Operation(summary = "음성 답안 제출 및 검증 (난이도 상)", description = "음성 파일을 FastAPI로 분석하여 정답 여부를 확인합니다.")
    @PostMapping("/submit-audio")
    public ApiResponse<AudioSubmitResponse> submitAudioAnswer(
            @Valid @RequestBody AudioSubmitRequest request) {
        AudioSubmitResponse response = scenarioSessionService.submitAudioAnswer(request);
        return ApiResponse.success(SuccessCode.AUDIO_ANSWER_CHECK_SUCCESS, response);
    }
     */

    @Operation(summary = "세션 완료", description = "진행 중인 세션을 완료 상태로 변경합니다.")
    @PostMapping("/complete")
    public ApiResponse<SessionCompleteResponse> completeSession(
            @Valid @RequestBody SessionCompleteRequest request) {
        SessionCompleteResponse response = scenarioSessionService.completeSession(request);
        return ApiResponse.success(SuccessCode.SESSION_COMPLETE_SUCCESS, response);
    }

    @Operation(summary = "오디오 답변 목록 조회", description = "특정 세션의 모든 오디오 답변을 시퀀스별로 그룹화하여 조회합니다.")
    @GetMapping("/audio-answers/{sessionId}")
    public ApiResponse<AudioAnswerListResponse> getAudioAnswers(@PathVariable Long sessionId) {
        AudioAnswerListResponse response = scenarioSessionService.getAudioAnswers(sessionId);
        return ApiResponse.success(SuccessCode.AUDIO_ANSWER_LIST_SUCCESS, response);
    }

    @Operation(summary = "S3 Key로 오디오 조회용 URL 생성", description = "S3 Key를 받아 오디오 파일 조회용 Pre-signed URL을 생성합니다. (FastAPI STT 분석용)")
    @PostMapping("/audio-url-by-key")
    public ApiResponse<AudioUrlByKeyResponse> generateAudioUrlByKey(
            @Valid @RequestBody AudioUrlByKeyRequest request) {
        AudioUrlByKeyResponse response = scenarioSessionService.generateAudioUrlByKey(request);
        return ApiResponse.success(SuccessCode.AUDIO_URL_BY_KEY_GENERATED, response);
    }
}