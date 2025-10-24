package com.ssafy.a202.domain.scenario.service;

import com.ssafy.a202.domain.scenario.dto.ScenarioResponse;
import com.ssafy.a202.domain.scenario.entity.Scenario;
import com.ssafy.a202.domain.scenario.repository.ScenarioRepository;
import com.ssafy.a202.global.exception.CustomException;
import com.ssafy.a202.global.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 시나리오 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScenarioServiceImpl implements ScenarioService {

    private final ScenarioRepository scenarioRepository;

    @Override
    public List<ScenarioResponse> getAllScenarios() {
        List<Scenario> scenarios = scenarioRepository.findByDeletedFalse();

        return scenarios.stream()
                .map(ScenarioResponse::from)
                .toList();
    }

    @Override
    public List<ScenarioResponse> getScenariosByCategory(Long categoryId) {
        List<Scenario> scenarios = scenarioRepository.findByScenarioCategoryIdAndDeletedFalse(categoryId);

        return scenarios.stream()
                .map(ScenarioResponse::from)
                .toList();
    }

    @Override
    public ScenarioResponse getScenarioById(Long scenarioId) {
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (scenario.isDeleted()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        return ScenarioResponse.from(scenario);
    }
}