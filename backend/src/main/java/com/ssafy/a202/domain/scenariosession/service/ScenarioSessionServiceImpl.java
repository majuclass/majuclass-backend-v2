package com.ssafy.a202.domain.scenariosession.service;

import com.ssafy.a202.domain.scenario.entity.Scenario;
import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import com.ssafy.a202.domain.scenario.entity.SeqOption;
import com.ssafy.a202.domain.scenario.repository.ScenarioRepository;
import com.ssafy.a202.domain.scenariosession.dto.AnswerCheckResponse;
import com.ssafy.a202.domain.scenariosession.dto.AnswerSubmitRequest;
import com.ssafy.a202.domain.scenariosession.dto.OptionResponse;
import com.ssafy.a202.domain.scenariosession.dto.SequenceResponse;
import com.ssafy.a202.domain.scenariosession.dto.SequenceWithOptionsResponse;
import com.ssafy.a202.global.exception.CustomException;
import com.ssafy.a202.global.constants.ErrorCode;
import com.ssafy.a202.global.s3.S3UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 시나리오 세션 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScenarioSessionServiceImpl implements ScenarioSessionService {

    private final ScenarioRepository scenarioRepository;
    private final S3UrlService s3UrlService;

    @Override
    public List<SequenceWithOptionsResponse> getScenarioSimulation(Long scenarioId) {
        // 시나리오 조회
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        // 삭제된 시나리오는 조회 불가
        if (scenario.isDeleted()) {
            throw new CustomException(ErrorCode.SCENARIO_NOT_FOUND);
        }

        log.info("Retrieved all sequences and options for scenario ID: {}", scenarioId);

        // 시퀀스를 순서대로 정렬해서 DTO로 변환 (시나리오 정보 제외)
        return scenario.getScenarioSequences().stream()
                .filter(seq -> !seq.isDeleted())  // 삭제되지 않은 시퀀스만
                .sorted((a, b) -> Integer.compare(a.getSeqNo(), b.getSeqNo()))  // seqNo 순서로 정렬
                .map(seq -> {
                    // 각 시퀀스의 미디어 URL 생성
                    String mediaUrl = s3UrlService.generateUrl(
                            seq.getSeqS3Key());
                    return SequenceWithOptionsResponse.from(seq, mediaUrl);
                })
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

        // 미디어 URL 생성
        String mediaUrl = s3UrlService.generateUrl(sequence.getSeqS3Key());

        log.info("Retrieved sequence {} for scenario ID: {}", sequenceNumber, scenarioId);

        return SequenceResponse.from(sequence, mediaUrl, hasNext);
    }

    @Override
    public List<OptionResponse> getSequenceOptions(Long scenarioId, int sequenceNumber) {
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

        log.info("Retrieved options for sequence {} in scenario ID: {}", sequenceNumber, scenarioId);

        // 옵션을 번호 순서대로 정렬해서 변환
        return sequence.getOptions().stream()
                .filter(option -> !option.isDeleted())
                .sorted((a, b) -> Integer.compare(a.getOptionNo(), b.getOptionNo()))
                .map(OptionResponse::from)
                .toList();
    }

    @Override
    public AnswerCheckResponse submitAnswer(AnswerSubmitRequest request) {
        log.info("Checking answer for scenario ID: {}, sequence number: {}, selected option ID: {}",
                request.getScenarioId(), request.getSequenceNumber(), request.getSelectedOptionId());

        // 시나리오 조회
        Scenario scenario = scenarioRepository.findById(request.getScenarioId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        // 삭제된 시나리오는 접근 불가
        if (scenario.isDeleted()) {
            throw new CustomException(ErrorCode.SCENARIO_NOT_FOUND);
        }

        // 해당 시퀀스 번호의 시퀀스 조회
        ScenarioSequence sequence = scenario.getScenarioSequences().stream()
                .filter(seq -> seq.getSeqNo() == request.getSequenceNumber() && !seq.isDeleted())
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.SEQUENCE_NOT_FOUND));

        // 사용자가 선택한 옵션 찾기
        SeqOption selectedOption = sequence.getOptions().stream()
                .filter(option -> option.getId().equals(request.getSelectedOptionId()) && !option.isDeleted())
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_OPTION_SELECTED));

        // 정답 여부 확인
        boolean isCorrect = selectedOption.isAnswer();

        log.info("Answer check result - scenarioId: {}, sequenceNumber: {}, isCorrect: {}, submitted option ID: {}",
                scenario.getId(), sequence.getSeqNo(), isCorrect, selectedOption.getId());

        return AnswerCheckResponse.builder()
                .scenarioId(scenario.getId())
                .sequenceId(sequence.getId())
                .sequenceNumber(sequence.getSeqNo())
                .submittedOptionId(selectedOption.getId())
                .isCorrect(isCorrect)
                .build();
    }
}