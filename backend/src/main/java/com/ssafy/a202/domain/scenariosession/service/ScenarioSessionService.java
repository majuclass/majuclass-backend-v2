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
//    AudioSubmitResponse submitAudioAnswer(AudioSubmitRequest request);

    /**
     * 세션 완료 처리
     *
     * @param request 세션 완료 요청
     * @return 세션 완료 응답
     */
    SessionCompleteResponse completeSession(SessionCompleteRequest request);

    /**
     * 특정 세션의 특정 시퀀스에 대한 오디오 답변 목록 조회
     *
     * @param sessionId 세션 ID
     * @param sequenceNumber 시퀀스 번호
     * @return 오디오 답변 목록 응답
     */
    AudioAnswerListResponse getAudioAnswers(Long sessionId, Integer sequenceNumber);

    /**
     * S3 Key로 오디오 파일 조회용 Pre-signed URL 생성
     * FastAPI STT 분석용
     *
     * @param request S3 Key 요청
     * @return 오디오 파일 Pre-signed URL 응답
     */
    AudioUrlByKeyResponse generateAudioUrlByKey(AudioUrlByKeyRequest request);
}