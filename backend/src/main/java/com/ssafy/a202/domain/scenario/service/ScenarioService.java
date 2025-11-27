package com.ssafy.a202.domain.scenario.service;

import com.ssafy.a202.common.entity.CustomException.CustomException;
import com.ssafy.a202.common.exception.ErrorCode;
import com.ssafy.a202.domain.category.entity.Category;
import com.ssafy.a202.domain.category.repository.CategoryRepository;
import com.ssafy.a202.domain.scenario.dto.request.OptionRequest;
import com.ssafy.a202.domain.scenario.dto.request.ScenarioCreateRequest;
import com.ssafy.a202.domain.scenario.dto.request.SequenceRequest;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioCreateResponse;
import com.ssafy.a202.domain.scenario.entity.Option;
import com.ssafy.a202.domain.scenario.entity.Scenario;
import com.ssafy.a202.domain.scenario.entity.Sequence;
import com.ssafy.a202.domain.scenario.repository.OptionRepository;
import com.ssafy.a202.domain.scenario.repository.ScenarioRepository;
import com.ssafy.a202.domain.scenario.repository.SequenceRepository;
import com.ssafy.a202.domain.user.entity.User;
import com.ssafy.a202.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScenarioService {
    private final ScenarioRepository scenarioRepository;
    private final SequenceRepository sequenceRepository;
    private final OptionRepository optionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;


    @Transactional
    public ScenarioCreateResponse create(Long userId, ScenarioCreateRequest request) {

        Category category;
        Scenario scenario;

        // todo: option에 이미지 혹은 텍스트 둘 중 하나는 들어오게 해야함
        // todo: 시나리오 썸네일, 배경 검증 null 아닐때.

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        scenario = Scenario.from(user, category, request);
        scenarioRepository.save(scenario);

        for (SequenceRequest seq : request.sequences()) {
            Sequence sequence = Sequence.from(scenario, seq);
            sequenceRepository.save(sequence);

            for (OptionRequest opt : seq.options()) {

                // 객관식 선지 이미지 있으면 검증 null 아닐
                Option option = Option.from(sequence, opt);
                optionRepository.save(option);
            }
        }
        return ScenarioCreateResponse.of(scenario);

    }
}
