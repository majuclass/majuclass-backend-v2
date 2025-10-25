package com.ssafy.a202.domain.scenario.service;

import com.ssafy.a202.domain.scenario.dto.ScenarioResponse;
import com.ssafy.a202.global.constants.Difficulty;

import java.util.List;

/**
 * 시나리오 서비스 인터페이스
 */
public interface ScenarioService {

    /**
     * 시나리오 목록 조회 (카테고리 및 난이도 필터링 옵션)
     * @param categoryId 카테고리 ID (optional)
     * @param difficulty 난이도 (optional)
     */
    List<ScenarioResponse> getAllScenarios(Long categoryId, Difficulty difficulty);

    /**
     * 시나리오 상세 조회
     */
    ScenarioResponse getScenarioById(Long scenarioId);
}