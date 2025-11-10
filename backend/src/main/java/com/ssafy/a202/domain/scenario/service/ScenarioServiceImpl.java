package com.ssafy.a202.domain.scenario.service;

import com.ssafy.a202.domain.scenario.dto.request.*;
import com.ssafy.a202.domain.scenario.dto.response.ImageUploadUrlResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioCreateResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioUpdateResponse;
import com.ssafy.a202.domain.scenario.entity.Scenario;
import com.ssafy.a202.domain.scenario.entity.ScenarioCategory;
import com.ssafy.a202.domain.scenario.entity.ScenarioSequence;
import com.ssafy.a202.domain.scenario.entity.SeqOption;
import com.ssafy.a202.domain.scenario.repository.ScenarioCategoryRepository;
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
import java.util.Map;

/**
 * 시나리오 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScenarioServiceImpl implements ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioCategoryRepository scenarioCategoryRepository;
    private final S3UrlService s3UrlService;

    @Override
    public ImageUploadUrlResponse generateImageUploadUrl(ImageUploadUrlRequest request) {
        log.info("Generating image upload URL for imageType: {}", request.getImageType());

        // 1. 이미지 타입에 따라 S3UrlService.ScenarioImageType 매핑
        S3UrlService.ScenarioImageType imageType;
        switch (request.getImageType()) {
            case THUMBNAIL:
                imageType = S3UrlService.ScenarioImageType.THUMBNAIL;
                break;
            case BACKGROUND:
                imageType = S3UrlService.ScenarioImageType.BACKGROUND;
                break;
            case OPTION:
                imageType = S3UrlService.ScenarioImageType.OPTION;
                break;
            default:
                throw new CustomException(ErrorCode.INVALID_REQUEST, "유효하지 않은 이미지 타입입니다.");
        }

        // 2. S3 키 자동 생성 (UUID + 확장자)
        String s3Key = s3UrlService.generateScenarioImageKey(imageType, request.getContentType());

        log.info("Generated S3 key: {}", s3Key);

        // 3. Lambda를 통해 업로드용 Presigned URL 발급
        Map<String, String> uploadUrlResponse = s3UrlService.generateUploadPresignedUrl(
                s3Key,
                "putObject",
                request.getContentType()
        );

        String presignedUrl = uploadUrlResponse.get("url");
        String returnedS3Key = uploadUrlResponse.get("fileName");

        log.info("Generated image upload URL: s3Key={}", returnedS3Key);

        // 4. 응답 생성
        return ImageUploadUrlResponse.of(presignedUrl, returnedS3Key);
    }

    @Override
    @Transactional
    public ScenarioCreateResponse createScenario(ScenarioCreateRequest request) {
        // 1. 카테고리 검증
        ScenarioCategory category = scenarioCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 2. S3 키 검증 및 설정
        String thumbnailS3Key = validateAndGetS3Key(request.getThumbnailS3Key(), "thumbnails");
        String backgroundS3Key = validateAndGetS3Key(request.getBackgroundS3Key(), "backgrounds");

        // 3. 시나리오 엔티티 생성 및 저장
        Scenario scenario = Scenario.builder()
                .scenarioCategory(category)
                .title(request.getTitle())
                .summary(request.getSummary())
                .thumbnailS3Key(thumbnailS3Key)
                .backgroundS3Key(backgroundS3Key)
                .totalSequences(request.getSequences().size())
                .isDeleted(false)
                .build();

        Scenario savedScenario = scenarioRepository.save(scenario);
        log.info("Scenario created with ID: {}", savedScenario.getId());

        // 4. 시퀀스 및 옵션 생성
        for (SequenceCreateRequest seqReq : request.getSequences()) {
            ScenarioSequence sequence = ScenarioSequence.builder()
                    .scenario(savedScenario)
                    .seqNo(seqReq.getSeqNo())
                    .question(seqReq.getQuestion())
                    .isDeleted(false)
                    .build();

            // 옵션 생성 (옵션 S3 키 검증)
            for (OptionCreateRequest optReq : seqReq.getOptions()) {
                // 옵션 S3 키 검증
                String optionS3Key = optReq.getOptionS3Key();
                if (optionS3Key == null || !optionS3Key.startsWith("scenarios/options/")) {
                    throw new CustomException(ErrorCode.INVALID_S3_KEY,
                            String.format("유효하지 않은 옵션 S3 키입니다: %s", optionS3Key));
                }

                SeqOption option = SeqOption.builder()
                        .scenarioSequence(sequence)
                        .optionNo(optReq.getOptionNo())
                        .optionText(optReq.getOptionText())
                        .optionS3Key(optionS3Key)
                        .isAnswer(optReq.getIsAnswer())
                        .isDeleted(false)
                        .build();
            }

            // CascadeType.ALL로 시퀀스와 옵션이 자동 저장됨
            savedScenario.getScenarioSequences().add(sequence);
        }

        log.info("Scenario created successfully with {} sequences", request.getSequences().size());

        // 5. 응답 생성
        String thumbnailUrl = s3UrlService.generateUrl(thumbnailS3Key);
        String backgroundUrl = s3UrlService.generateUrl(backgroundS3Key);

        return ScenarioCreateResponse.from(savedScenario, thumbnailUrl, backgroundUrl);
    }

    /**
     * S3 키 검증 (선택 필드)
     */
    private String validateAndGetS3Key(String s3Key, String expectedPrefix) {
        // null이거나 빈 문자열이면 빈 문자열 반환 (선택 필드)
        if (s3Key == null || s3Key.trim().isEmpty()) {
            return "";
        }

        // scenarios/{expectedPrefix}/ 로 시작하는지 검증
        String expectedPath = "scenarios/" + expectedPrefix + "/";
        if (!s3Key.startsWith(expectedPath)) {
            throw new CustomException(ErrorCode.INVALID_S3_KEY,
                    String.format("유효하지 않은 S3 키입니다. 예상: %s, 실제: %s", expectedPath, s3Key));
        }

        return s3Key;
    }

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

        log.debug("Retrieved all sequences and options for scenario ID: {}", scenarioId);

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

        log.debug("Retrieved sequence {} for scenario ID: {}", sequenceNumber, scenarioId);

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

        log.debug("Retrieved options for sequence {} in scenario ID: {} with difficulty: {}",
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

    @Override
    @Transactional
    public ScenarioUpdateResponse updateScenario(Long scenarioId, ScenarioUpdateRequest request) {
        log.info("Updating scenario ID: {}", scenarioId);

        // 1. 시나리오 조회 및 검증
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        if (scenario.isDeleted()) {
            throw new CustomException(ErrorCode.SCENARIO_NOT_FOUND, "삭제된 시나리오는 수정할 수 없습니다.");
        }

        // 2. 카테고리 조회
        ScenarioCategory category = scenarioCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 3. 기본 정보 수정
        scenario.updateBasicInfo(request.getTitle(), request.getSummary(), category);

        // 4. 이미지 수정 (null이 아닌 경우에만)
        if (request.getThumbnailS3Key() != null) {
            String thumbnailS3Key = validateAndGetS3Key(request.getThumbnailS3Key(), "thumbnails");
            scenario.updateThumbnail(thumbnailS3Key);
        }

        if (request.getBackgroundS3Key() != null) {
            String backgroundS3Key = validateAndGetS3Key(request.getBackgroundS3Key(), "backgrounds");
            scenario.updateBackground(backgroundS3Key);
        }

        // 5. 시퀀스 처리
        List<SequenceUpdateRequest> requestSequences = request.getSequences();
        List<ScenarioSequence> existingSequences = scenario.getScenarioSequences();

        // 요청에 포함된 시퀀스 ID 목록
        List<Long> requestSequenceIds = requestSequences.stream()
                .map(SequenceUpdateRequest::getId)
                .filter(id -> id != null)
                .toList();

        // 기존 시퀀스 중 요청에 없는 것들 soft delete
        existingSequences.stream()
                .filter(seq -> !seq.isDeleted() && !requestSequenceIds.contains(seq.getId()))
                .forEach(ScenarioSequence::delete);

        // 시퀀스 수정 또는 생성
        for (SequenceUpdateRequest seqReq : requestSequences) {
            if (seqReq.getId() != null) {
                // 기존 시퀀스 수정
                ScenarioSequence existingSeq = existingSequences.stream()
                        .filter(seq -> seq.getId().equals(seqReq.getId()))
                        .findFirst()
                        .orElseThrow(() -> new CustomException(ErrorCode.SEQUENCE_NOT_FOUND,
                                String.format("시퀀스 ID %d를 찾을 수 없습니다.", seqReq.getId())));

                existingSeq.updateQuestion(seqReq.getQuestion());

                // 옵션 처리
                updateSequenceOptions(existingSeq, seqReq.getOptions());
            } else {
                // 신규 시퀀스 생성
                ScenarioSequence newSequence = ScenarioSequence.builder()
                        .scenario(scenario)
                        .seqNo(seqReq.getSeqNo())
                        .question(seqReq.getQuestion())
                        .isDeleted(false)
                        .build();

                // 옵션 생성
                for (OptionUpdateRequest optReq : seqReq.getOptions()) {
                    String optionS3Key = optReq.getOptionS3Key();
                    if (optionS3Key == null || !optionS3Key.startsWith("scenarios/options/")) {
                        throw new CustomException(ErrorCode.INVALID_S3_KEY,
                                String.format("유효하지 않은 옵션 S3 키입니다: %s", optionS3Key));
                    }

                    SeqOption newOption = SeqOption.builder()
                            .scenarioSequence(newSequence)
                            .optionNo(optReq.getOptionNo())
                            .optionText(optReq.getOptionText())
                            .optionS3Key(optionS3Key)
                            .isAnswer(optReq.getIsAnswer())
                            .isDeleted(false)
                            .build();
                }

                scenario.getScenarioSequences().add(newSequence);
            }
        }

        // 6. 총 시퀀스 개수 업데이트 (삭제되지 않은 시퀀스만 카운트)
        int activeSequences = (int) scenario.getScenarioSequences().stream()
                .filter(seq -> !seq.isDeleted())
                .count();
        scenario.updateTotalSequences(activeSequences);

        log.info("Scenario updated successfully: ID={}", scenarioId);

        // 7. 응답 생성
        String thumbnailUrl = s3UrlService.generateUrl(scenario.getThumbnailS3Key());
        String backgroundUrl = s3UrlService.generateUrl(scenario.getBackgroundS3Key());

        return ScenarioUpdateResponse.from(scenario, thumbnailUrl, backgroundUrl);
    }

    /**
     * 시퀀스의 옵션 수정 처리
     */
    private void updateSequenceOptions(ScenarioSequence sequence, List<OptionUpdateRequest> requestOptions) {
        List<SeqOption> existingOptions = sequence.getOptions();

        // 요청에 포함된 옵션 ID 목록
        List<Long> requestOptionIds = requestOptions.stream()
                .map(OptionUpdateRequest::getId)
                .filter(id -> id != null)
                .toList();

        // 기존 옵션 중 요청에 없는 것들 soft delete
        existingOptions.stream()
                .filter(opt -> !opt.isDeleted() && !requestOptionIds.contains(opt.getId()))
                .forEach(SeqOption::delete);

        // 옵션 수정 또는 생성
        for (OptionUpdateRequest optReq : requestOptions) {
            // 옵션 S3 키 검증
            String optionS3Key = optReq.getOptionS3Key();
            if (optionS3Key == null || !optionS3Key.startsWith("scenarios/options/")) {
                throw new CustomException(ErrorCode.INVALID_S3_KEY,
                        String.format("유효하지 않은 옵션 S3 키입니다: %s", optionS3Key));
            }

            if (optReq.getId() != null) {
                // 기존 옵션 수정
                SeqOption existingOpt = existingOptions.stream()
                        .filter(opt -> opt.getId().equals(optReq.getId()))
                        .findFirst()
                        .orElseThrow(() -> new CustomException(ErrorCode.OPTION_NOT_FOUND,
                                String.format("옵션 ID %d를 찾을 수 없습니다.", optReq.getId())));

                existingOpt.updateOption(optReq.getOptionText(), optionS3Key, optReq.getIsAnswer());
            } else {
                // 신규 옵션 생성
                SeqOption newOption = SeqOption.builder()
                        .scenarioSequence(sequence)
                        .optionNo(optReq.getOptionNo())
                        .optionText(optReq.getOptionText())
                        .optionS3Key(optionS3Key)
                        .isAnswer(optReq.getIsAnswer())
                        .isDeleted(false)
                        .build();

                sequence.getOptions().add(newOption);
            }
        }
    }

    @Override
    @Transactional
    public void deleteScenario(Long scenarioId) {
        log.info("Deleting scenario ID: {}", scenarioId);

        // 시나리오 조회 및 검증
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        if (scenario.isDeleted()) {
            throw new CustomException(ErrorCode.SCENARIO_NOT_FOUND, "이미 삭제된 시나리오입니다.");
        }

        // soft delete 수행
        scenario.delete();

        log.info("Scenario deleted successfully: ID={}", scenarioId);
    }
}