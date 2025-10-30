package com.ssafy.a202.domain.scenario.service;

import com.ssafy.a202.domain.scenario.dto.ScenarioResponse;
import com.ssafy.a202.domain.scenario.entity.Scenario;
import com.ssafy.a202.domain.scenario.repository.ScenarioRepository;
import com.ssafy.a202.global.constants.Difficulty;
import com.ssafy.a202.global.exception.CustomException;
import com.ssafy.a202.global.constants.ErrorCode;
import com.ssafy.a202.global.s3.S3UrlService;
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
    private final S3UrlService s3UrlService;

    @Override
    public List<ScenarioResponse> getAllScenarios(Long categoryId, Difficulty difficulty) {
        List<Scenario> scenarios;

        // 카테고리와 난이도 모두 있는 경우
        if (categoryId != null && difficulty != null) {
            scenarios = scenarioRepository.findByScenarioCategoryIdAndDifficultyAndIsDeletedFalse(categoryId, difficulty);
        }
        // 카테고리만 있는 경우
        else if (categoryId != null) {
            scenarios = scenarioRepository.findByScenarioCategoryIdAndIsDeletedFalse(categoryId);
        }
        // 난이도만 있는 경우
        else if (difficulty != null) {
            scenarios = scenarioRepository.findByDifficultyAndIsDeletedFalse(difficulty);
        }
        // 둘 다 없는 경우 (전체 조회)
        else {
            scenarios = scenarioRepository.findByIsDeletedFalse();
        }

        return scenarios.stream()
                .map(scenario -> {
                    String thumbnailUrl = s3UrlService.generateUrl(
                            scenario.getThumbnailS3Key());
                    return ScenarioResponse.from(scenario, thumbnailUrl);
                })
                .toList();
    }

    @Override
    public ScenarioResponse getScenarioById(Long scenarioId) {
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (scenario.isDeleted()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        String thumbnailUrl = s3UrlService.generateUrl(
                scenario.getThumbnailS3Key());

        return ScenarioResponse.from(scenario, thumbnailUrl);
    }
}