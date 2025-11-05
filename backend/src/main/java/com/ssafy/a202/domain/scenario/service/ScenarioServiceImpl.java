package com.ssafy.a202.domain.scenario.service;

import com.ssafy.a202.domain.scenario.dto.response.ScenarioResponse;
import com.ssafy.a202.domain.scenario.entity.Scenario;
import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import com.ssafy.a202.domain.scenario.repository.ScenarioRepository;
import com.ssafy.a202.domain.scenario.dto.response.OptionResponse;
import com.ssafy.a202.domain.scenario.dto.response.OptionWithImageResponse;
import com.ssafy.a202.domain.scenario.dto.response.SequenceResponse;
import com.ssafy.a202.domain.scenario.dto.response.SequenceWithOptionsResponse;
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
    public List<ScenarioResponse> getAllScenarios(Long categoryId) {
        List<Scenario> scenarios;

        // 카테고리가 있는 경우
        if (categoryId != null) {
            scenarios = scenarioRepository.findByScenarioCategoryIdAndIsDeletedFalse(categoryId);
        }
        // 전체 조회
        else {
            scenarios = scenarioRepository.findByIsDeletedFalse();
        }

        return scenarios.stream()
                .map(scenario -> {
                    String thumbnailUrl = s3UrlService.generateUrl(scenario.getThumbnailS3Key());
                    return ScenarioResponse.fromList(scenario, thumbnailUrl);
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

        String backgroundUrl = s3UrlService.generateUrl(scenario.getBackgroundS3Key());

        return ScenarioResponse.fromDetail(scenario, backgroundUrl);
    }

    @Override
    public List<SequenceWithOptionsResponse> getAllSequencesWithOptions(Long scenarioId) {
        // 시나리오 조회
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        // 삭제된 시나리오는 조회 불가
        if (scenario.isDeleted()) {
            throw new CustomException(ErrorCode.SCENARIO_NOT_FOUND);
        }

        log.info("Retrieved all sequences and options for scenario ID: {}", scenarioId);

        // 시퀀스를 순서대로 정렬해서 DTO로 변환
        return scenario.getScenarioSequences().stream()
                .filter(seq -> !seq.isDeleted())  // 삭제되지 않은 시퀀스만
                .sorted((a, b) -> Integer.compare(a.getSeqNo(), b.getSeqNo()))  // seqNo 순서로 정렬
                .map(SequenceWithOptionsResponse::from)
                .toList();
    }

    @Override
    public SequenceResponse getSequence(Long scenarioId, int sequenceNumber) {
        // 시나리오 조회
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        // 삭제된 시나리오는 조회 불가
        if (scenario.isDeleted()) {
            throw new CustomException(ErrorCode.SCENARIO_NOT_FOUND);
        }

        // 해당 시퀀스 번호의 시퀀스 조회
        ScenarioSequence sequence = scenario.getScenarioSequences().stream()
                .filter(seq -> seq.getSeqNo() == sequenceNumber && !seq.isDeleted())
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.SEQUENCE_NOT_FOUND));

        // 다음 시퀀스 존재 여부 확인
        boolean hasNext = scenario.getScenarioSequences().stream()
                .anyMatch(seq -> seq.getSeqNo() == sequenceNumber + 1 && !seq.isDeleted());

        log.info("Retrieved sequence {} for scenario ID: {}", sequenceNumber, scenarioId);

        return SequenceResponse.from(sequence, hasNext);
    }

    @Override
    public List<?> getSequenceOptions(Long scenarioId, int sequenceNumber, String difficulty) {
        // 난이도 검증
        if (!difficulty.equals("EASY") && !difficulty.equals("NORMAL") && !difficulty.equals("HARD")) {
            throw new CustomException(ErrorCode.INVALID_DIFFICULTY);
        }

        // 시나리오 조회
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        // 삭제된 시나리오는 조회 불가
        if (scenario.isDeleted()) {
            throw new CustomException(ErrorCode.SCENARIO_NOT_FOUND);
        }

        // 해당 시퀀스 번호의 시퀀스 조회
        ScenarioSequence sequence = scenario.getScenarioSequences().stream()
                .filter(seq -> seq.getSeqNo() == sequenceNumber && !seq.isDeleted())
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.SEQUENCE_NOT_FOUND));

        log.info("Retrieved options for sequence {} in scenario ID: {} with difficulty: {}",
                sequenceNumber, scenarioId, difficulty);

        // EASY 난이도: 이미지 URL 포함
        if (difficulty.equals("EASY")) {
            return sequence.getOptions().stream()
                    .filter(option -> !option.isDeleted())
                    .sorted((a, b) -> Integer.compare(a.getOptionNo(), b.getOptionNo()))
                    .map(option -> {
                        String imageUrl = s3UrlService.generateUrl(option.getOptionS3Key());
                        return OptionWithImageResponse.from(option, imageUrl);
                    })
                    .toList();
        }

        // NORMAL, HARD 난이도: 텍스트만 포함
        return sequence.getOptions().stream()
                .filter(option -> !option.isDeleted())
                .sorted((a, b) -> Integer.compare(a.getOptionNo(), b.getOptionNo()))
                .map(OptionResponse::from)
                .toList();
    }
}