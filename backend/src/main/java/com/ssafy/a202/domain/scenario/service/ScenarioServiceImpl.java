package com.ssafy.a202.domain.scenario.service;

import com.ssafy.a202.common.entity.PageResponse;
import com.ssafy.a202.common.exception.CustomException;
import com.ssafy.a202.common.exception.ErrorCode;
import com.ssafy.a202.common.roleAop.PermissionAction;
import com.ssafy.a202.common.roleAop.scenarioPermission.CheckScenarioPermission;
import com.ssafy.a202.domain.category.entity.Category;
import com.ssafy.a202.domain.category.repository.CategoryRepository;
import com.ssafy.a202.domain.presignedUrl.client.S3Client;
import com.ssafy.a202.domain.scenario.dto.request.OptionRequest;
import com.ssafy.a202.domain.scenario.dto.request.ScenarioRequest;
import com.ssafy.a202.domain.scenario.dto.request.SequenceRequest;
import com.ssafy.a202.domain.scenario.dto.response.*;
import com.ssafy.a202.domain.scenario.entity.Option;
import com.ssafy.a202.domain.scenario.entity.Scenario;
import com.ssafy.a202.domain.scenario.entity.Sequence;
import com.ssafy.a202.domain.scenario.repository.OptionRepository;
import com.ssafy.a202.domain.scenario.repository.ScenarioRepository;
import com.ssafy.a202.domain.scenario.repository.SequenceRepository;
import com.ssafy.a202.domain.user.entity.User;
import com.ssafy.a202.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScenarioServiceImpl implements ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final SequenceRepository sequenceRepository;
    private final OptionRepository optionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final S3Client s3Client;


    @Transactional
    @Override
    public ScenarioCreateResponse create(Long userId, ScenarioRequest request) {

        Category category;
        Scenario scenario;

        // todo: option에 이미지 혹은 텍스트 둘 중 하나는 들어오게 해야함.

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 배경 이미지가 있다면 S3에 존재하는지 확인
        if (request.backgroundS3Key() != null)
            s3Client.validateS3FileExists(request.backgroundS3Key());

        // 썸네일 이미지가 있다면 S3에 존재하는지 확인
        if (request.thumbnailS3Key() != null)
            s3Client.validateS3FileExists(request.thumbnailS3Key());

        scenario = Scenario.from(user, category, request);
        scenarioRepository.save(scenario);

        for (SequenceRequest seq : request.sequences()) {
            Sequence sequence = Sequence.from(scenario, seq);
            sequenceRepository.save(sequence);

            for (OptionRequest opt : seq.options()) {

                // 객관식 이미지가 있으면 존재하는지 확인
                if (opt.optionS3Key() != null) {
                    s3Client.validateS3FileExists(opt.optionS3Key());
                }

                Option option = Option.from(sequence, opt);
                optionRepository.save(option);
            }
        }
        return ScenarioCreateResponse.of(scenario);
    }

    @Override
    public PageResponse<ScenarioPreviewResponse> getScenarios(Pageable pageable) {
        Page<Scenario> scenarioPage = scenarioRepository.findByDeletedAtIsNull(pageable);

        List<ScenarioPreviewResponse> responseList = new ArrayList<>();

        for (Scenario scenario : scenarioPage.getContent()) {
            String thumbnailUrl = null;
            String backgroundUrl = null;

            if (scenario.getThumbnailS3Key() != null)
                thumbnailUrl = s3Client.getPublicS3Url(scenario.getThumbnailS3Key());
            if (scenario.getBackgroundS3Key() != null)
                backgroundUrl = s3Client.getPublicS3Url(scenario.getBackgroundS3Key());

            ScenarioPreviewResponse response =  ScenarioPreviewResponse.of(scenario, thumbnailUrl, backgroundUrl);
            responseList.add(response);
        }
        return PageResponse.of(scenarioPage, responseList);
    }

    @Override
    public ScenarioDetailResponse getSingleScenario(Long scenarioId) {
        Scenario scenario = scenarioRepository.findByIdAndDeletedAtIsNull(scenarioId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        // S3 URL 변환
        String thumbnailUrl = null;
        String backgroundUrl = null;

        if (scenario.getThumbnailS3Key() != null)
            thumbnailUrl = s3Client.getPublicS3Url(scenario.getThumbnailS3Key());
        if (scenario.getBackgroundS3Key() != null)
            backgroundUrl = s3Client.getPublicS3Url(scenario.getBackgroundS3Key());

        // Sequence 조회 및 변환
        List<Sequence> sequences = sequenceRepository.findByScenarioIdAndDeletedAtIsNull(scenario.getId());
        List<SequenceResponse> sequenceResponses = new ArrayList<>();

        for (Sequence sequence : sequences) {
            List<Option> options = optionRepository.findBySequenceIdAndDeletedAtIsNull(sequence.getId());
            List<OptionResponse> optionResponses = new ArrayList<>();

            for (Option option : options) {
                String optionUrl = null;
                if (option.getOptionS3Key() != null) {
                    optionUrl = s3Client.getPublicS3Url(option.getOptionS3Key());
                }
                optionResponses.add(OptionResponse.of(option, optionUrl));
            }

            sequenceResponses.add(SequenceResponse.of(sequence, optionResponses));
        }

        return ScenarioDetailResponse.of(scenario, thumbnailUrl, backgroundUrl, sequenceResponses);
    }

    @CheckScenarioPermission(PermissionAction.UPDATE)
    @Transactional
    @Override
    public void update(Long userId, Long scenarioId, ScenarioRequest request) {
        // 1. Scenario 조회
        Scenario scenario = scenarioRepository.findByIdAndDeletedAtIsNull(scenarioId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        // 2. Category 조회
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 3. S3 키 검증
        if (request.thumbnailS3Key() != null)
            s3Client.validateS3FileExists(request.thumbnailS3Key());
        if (request.backgroundS3Key() != null)
            s3Client.validateS3FileExists(request.backgroundS3Key());

        // 4. Scenario 업데이트
        scenario.update(category, request);

        // 5. 기존 Sequence와 Option 소프트 딜리트
        softDeleteSequencesAndOptionsByScenarioId(scenarioId);

        // 6. 새로운 Sequence와 Option 생성
        for (SequenceRequest seq : request.sequences()) {
            Sequence sequence = Sequence.from(scenario, seq);
            sequenceRepository.save(sequence);

            for (OptionRequest opt : seq.options()) {
                if (opt.optionS3Key() != null) {
                    s3Client.validateS3FileExists(opt.optionS3Key());
                }
                Option option = Option.from(sequence, opt);
                optionRepository.save(option);
            }
        }
    }

    @CheckScenarioPermission(PermissionAction.DELETE)
    @Transactional
    @Override
    public void delete(Long userId, Long scenarioId) {
        // 1. Scenario 조회
        Scenario scenario = scenarioRepository.findByIdAndDeletedAtIsNull(scenarioId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCENARIO_NOT_FOUND));

        // 2. 연관된 Sequence와 Option 소프트 딜리트
        softDeleteSequencesAndOptionsByScenarioId(scenarioId);

        // 3. Scenario 소프트 딜리트
        scenario.delete();
    }

    /**
     * 시나리오에 속한 모든 시퀀스와 옵션을 소프트 삭제합니다.
     *
     * <p>이 메서드는 특정 시나리오에 연결된 모든 학습 데이터를 안전하게 제거하기 위해 사용됩니다.
     * 실제 데이터베이스에서 레코드를 삭제하는 대신 {@code deleted_at} 타임스탬프를 설정하여
     * 데이터 무결성을 유지하면서 논리적으로 삭제합니다.</p>
     *
     * <p><b>동작 흐름:</b></p>
     * <ol>
     *   <li>시나리오 ID로 모든 활성 시퀀스 조회</li>
     *   <li>각 시퀀스에 속한 모든 활성 옵션 소프트 삭제</li>
     *   <li>시퀀스 소프트 삭제</li>
     * </ol>
     *
     * <p><b>주의사항:</b></p>
     * <ul>
     *   <li>이 메서드는 트랜잭션 내에서 실행되어야 합니다</li>
     *   <li>학생 학습 기록과의 무결성을 위해 실제 삭제가 아닌 소프트 삭제를 사용합니다</li>
     * </ul>
     *
     * @param scenarioId 삭제할 시나리오의 ID (NotNull)
     *
     * @throws CustomException 시나리오를 찾을 수 없거나 삭제 처리 중 오류가 발생한 경우
     *
     * @since 1.0
     */
    private void softDeleteSequencesAndOptionsByScenarioId(Long scenarioId) {
        List<Sequence> sequences = sequenceRepository.findByScenarioIdAndDeletedAtIsNull(scenarioId);
        for (Sequence sequence : sequences) {
            // Option 소프트 딜리트
            List<Option> options = optionRepository.findBySequenceIdAndDeletedAtIsNull(sequence.getId());
            for (Option option : options) {
                option.delete();
            }
            // Sequence 소프트 딜리트
            sequence.delete();
        }
    }


}
