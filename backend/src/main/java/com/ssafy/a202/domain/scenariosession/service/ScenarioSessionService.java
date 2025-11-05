package com.ssafy.a202.domain.scenariosession.service;

import com.ssafy.a202.domain.scenariosession.dto.request.*;
import com.ssafy.a202.domain.scenariosession.dto.response.*;

/**
 * 시나리오 세션 서비스 인터페이스
 */
public interface ScenarioSessionService {

    /**
     * 세션 시작
     *
     * @param request 세션 시작 요청
     * @return 세션 시작 응답
     */
    SessionStartResponse startSession(SessionStartRequest request);

    /**
     * 음성 업로드 URL 생성
     *
     * @param request 음성 업로드 URL 요청 (세션 ID, 시퀀스 번호 포함)
     * @return 음성 업로드 URL 응답
     */
    AudioUploadUrlResponse generateAudioUploadUrl(AudioUploadUrlRequest request);

    /**
     * 답안 제출 및 검증 (난이도 하/중)
     *
     * @param request 답안 제출 요청
     * @return 답안 검증 결과
     */
    AnswerCheckResponse submitAnswer(AnswerSubmitRequest request);

    /**
     * 음성 답안 제출 및 검증 (난이도 상)
     * FastAPI STT 분석 후 정답 여부 판정
     *
     * @param request 음성 답안 제출 요청
     * @return 음성 답안 검증 결과
     */
    AudioSubmitResponse submitAudioAnswer(AudioSubmitRequest request);

    /**
     * 세션 완료 처리
     *
     * @param request 세션 완료 요청
     * @return 세션 완료 응답
     */
    SessionCompleteResponse completeSession(SessionCompleteRequest request);
}