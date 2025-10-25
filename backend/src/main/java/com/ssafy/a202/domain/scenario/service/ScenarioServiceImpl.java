package com.ssafy.a202.domain.scenario.service;

import com.ssafy.a202.domain.scenario.dto.ScenarioResponse;
import com.ssafy.a202.domain.scenario.entity.Scenario;
import com.ssafy.a202.domain.scenario.repository.ScenarioRepository;
import com.ssafy.a202.global.exception.CustomException;
import com.ssafy.a202.global.constants.ErrorCode;
import com.ssafy.a202.global.s3.S3PresignedUrlProvider;
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
    private final S3PresignedUrlProvider s3PresignedUrlProvider;

    @Override
    public List<ScenarioResponse> getAllScenarios() {
        List<Scenario> scenarios = scenarioRepository.findByIsDeletedFalse();

        return scenarios.stream()
                .map(scenario -> {
                    String thumbnailUrl = s3PresignedUrlProvider.generatePresignedUrl(
                            scenario.getThumbnailS3Key());
                    return ScenarioResponse.from(scenario, thumbnailUrl);
                })
                .toList();
    }

    @Override
    public List<ScenarioResponse> getScenariosByCategory(Long categoryId) {
        List<Scenario> scenarios = scenarioRepository.findByScenarioCategoryIdAndIsDeletedFalse(categoryId);

        return scenarios.stream()
                .map(scenario -> {
                    String thumbnailUrl = s3PresignedUrlProvider.generatePresignedUrl(
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

        String thumbnailUrl = s3PresignedUrlProvider.generatePresignedUrl(
                scenario.getThumbnailS3Key());

        return ScenarioResponse.from(scenario, thumbnailUrl);
    }
}