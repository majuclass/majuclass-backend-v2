package com.ssafy.a202.domain.scenario.service;

import com.ssafy.a202.domain.scenario.dto.request.ImageUploadUrlRequest;
import com.ssafy.a202.domain.scenario.dto.request.ScenarioCreateRequest;
import com.ssafy.a202.domain.scenario.dto.response.ImageUploadUrlResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioCreateResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioResponse;
import com.ssafy.a202.domain.scenario.dto.response.SequenceResponse;
import com.ssafy.a202.domain.scenario.dto.response.SequenceWithOptionsResponse;

import java.util.List;

/**
 * 시나리오 서비스 인터페이스
 */
public interface ScenarioService {

    /**
     * 이미지 업로드용 Presigned URL 발급
     * @param request 이미지 업로드 URL 요청 (imageType, filename, contentType)
     * @return Presigned URL과 S3 키
     */
    ImageUploadUrlResponse generateImageUploadUrl(ImageUploadUrlRequest request);

    /**
     * 시나리오 생성
     * @param request 시나리오 생성 요청 데이터 (S3 키 포함)
     */
    ScenarioCreateResponse createScenario(ScenarioCreateRequest request);

    /**
     * 시나리오 목록 조회 (카테고리 필터링 옵션, 썸네일 URL 포함)
     * @param categoryId 카테고리 ID (optional)
     */
    List<ScenarioResponse> getAllScenarios(Long categoryId);

    /**
     * 시나리오 상세 조회 (백그라운드 이미지 URL 포함)
     */
    ScenarioResponse getScenarioById(Long scenarioId);

    /**
     * 시나리오의 모든 시퀀스와 옵션을 한번에 조회 (일괄 조회 방식)
     * - 개별 API 호출 대비 성능 비교를 위해 제공
     */
    List<SequenceWithOptionsResponse> getAllSequencesWithOptions(Long scenarioId);

    /**
     * 특정 시퀀스 조회 (개별 조회 방식)
     */
    SequenceResponse getSequence(Long scenarioId, int sequenceNumber);

    /**
     * 특정 시퀀스의 옵션 조회 (개별 조회 방식, 난이도에 따라 다른 응답)
     */
    List<?> getSequenceOptions(Long scenarioId, int sequenceNumber, String difficulty);
}