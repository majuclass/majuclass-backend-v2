package com.ssafy.a202.domain.scenariosession.service;

import com.ssafy.a202.domain.scenariosession.dto.AnswerCheckResponse;
import com.ssafy.a202.domain.scenariosession.dto.AnswerSubmitRequest;
import com.ssafy.a202.domain.scenariosession.dto.OptionResponse;
import com.ssafy.a202.domain.scenariosession.dto.SequenceResponse;
import com.ssafy.a202.domain.scenariosession.dto.SequenceWithOptionsResponse;

import java.util.List;

/**
 * 시나리오 세션 서비스 인터페이스
 */
public interface ScenarioSessionService {

    /**
     * 시나리오의 모든 시퀀스와 옵션 조회
     * 시나리오의 모든 시퀀스와 옵션을 프리사인드 URL 포함해서 반환
     *
     * @param scenarioId 시나리오 ID
     * @return 시퀀스 및 옵션 목록 (시나리오 정보 제외)
     */
    List<SequenceWithOptionsResponse> getScenarioSimulation(Long scenarioId);

    /**
     * 특정 시퀀스 조회 (옵션 제외)
     *
     * @param scenarioId 시나리오 ID
     * @param sequenceNumber 시퀀스 번호 (1부터 시작)
     * @return 시퀀스 응답 (다음 시퀀스 존재 여부 포함)
     */
    SequenceResponse getSequence(Long scenarioId, int sequenceNumber);

    /**
     * 특정 시퀀스의 옵션 조회
     *
     * @param scenarioId 시나리오 ID
     * @param sequenceNumber 시퀀스 번호 (1부터 시작)
     * @return 옵션 응답 목록
     */
    List<OptionResponse> getSequenceOptions(Long scenarioId, int sequenceNumber);

    /**
     * 답안 제출 및 검증
     *
     * @param request 답안 제출 요청
     * @return 답안 검증 결과
     */
    AnswerCheckResponse submitAnswer(AnswerSubmitRequest request);
}