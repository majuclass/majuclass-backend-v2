package com.ssafy.a202.domain.scenario.service;

import com.ssafy.a202.common.client.s3.S3Client;
import com.ssafy.a202.common.entity.PageResponse;
import com.ssafy.a202.common.exception.CustomException;
import com.ssafy.a202.common.exception.ErrorCode;
import com.ssafy.a202.common.security.roleAop.CheckScenarioPermission;
import com.ssafy.a202.common.security.roleAop.PermissionAction;
import com.ssafy.a202.domain.category.entity.Category;
import com.ssafy.a202.domain.category.repository.CategoryRepository;
import com.ssafy.a202.domain.scenario.dto.request.OptionRequest;
import com.ssafy.a202.domain.scenario.dto.request.ScenarioRequest;
import com.ssafy.a202.domain.scenario.dto.request.SequenceRequest;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioCreateResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioDetailResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioPreviewResponse;
import com.ssafy.a202.domain.scenario.dto.response.SequenceResponse;
import com.ssafy.a202.domain.scenario.dto.response.OptionResponse;
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
public class ScenarioService {
    private final ScenarioRepository scenarioRepository;
    private final SequenceRepository sequenceRepository;
    private final OptionRepository optionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final S3Client s3Client;


    @Transactional
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

    public PageResponse<ScenarioPreviewResponse> getScenarios(Pageable pageable) {
        Page<Scenario> scenarioPage = scenarioRepository.findByDeletedAtIsNull(pageable);

        List<ScenarioPreviewResponse> responseList = new ArrayList<>();

        for (Scenario scenario : scenarioPage.getContent()) {
//            ScenarioPreviewResponse response = toScenarioPreviewResponse(scenario);
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
        List<Sequence> existingSequences = sequenceRepository.findByScenarioIdAndDeletedAtIsNull(scenarioId);
        for (Sequence seq : existingSequences) {
            // 먼저 Sequence에 속한 Option들 소프트 딜리트
            List<Option> options = optionRepository.findBySequenceIdAndDeletedAtIsNull(seq.getId());
            for (Option option : options) {
                option.delete();
            }
            // 그 다음 Sequence 소프트 딜리트
            seq.delete();
        }

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
    public void delete(Long userId, Long scenarioId) {

    }
}
