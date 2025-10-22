package com.ssafy.a202.domain.scenario.service;

import com.ssafy.a202.domain.scenario.dto.ScenarioResponse;

import java.util.List;

/**
 * 시나리오 서비스 인터페이스
 */
public interface ScenarioService {

    /**
     * 전체 시나리오 목록 조회
     */
    List<ScenarioResponse> getAllScenarios();

    /**
     * 카테고리별 시나리오 목록 조회
     */
    List<ScenarioResponse> getScenariosByCategory(Long categoryId);

    /**
     * 시나리오 상세 조회
     */
    ScenarioResponse getScenarioById(Long scenarioId);
}