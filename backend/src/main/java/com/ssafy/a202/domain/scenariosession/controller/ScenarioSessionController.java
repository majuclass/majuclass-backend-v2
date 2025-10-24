package com.ssafy.a202.domain.scenariosession.controller;

import com.ssafy.a202.domain.scenariosession.dto.OptionResponse;
import com.ssafy.a202.domain.scenariosession.dto.SequenceResponse;
import com.ssafy.a202.domain.scenariosession.dto.SequenceWithOptionsResponse;
import com.ssafy.a202.domain.scenariosession.service.ScenarioSessionService;
import com.ssafy.a202.global.constants.SuccessCode;
import com.ssafy.a202.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 시나리오 세션 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/scenario-sessions")
@RequiredArgsConstructor
@Tag(name = "ScenarioSession", description = "시나리오 세션 관련 API")
public class ScenarioSessionController {

    private final ScenarioSessionService scenarioSessionService;

    @Operation(summary = "시나리오 시뮬레이션 조회 (전체)", description = "시나리오의 모든 시퀀스와 옵션을 순차적으로 반환합니다. (프리사인드 URL 포함)")
    @GetMapping("/simulation/{scenarioId}")
    public ApiResponse<List<SequenceWithOptionsResponse>> getScenarioSimulation(
            @Parameter(description = "시나리오 ID", example = "1")
            @PathVariable Long scenarioId) {
        log.info("Fetching scenario simulation for scenario ID: {}", scenarioId);
        List<SequenceWithOptionsResponse> simulation = scenarioSessionService.getScenarioSimulation(scenarioId);
        return ApiResponse.success(SuccessCode.SCENARIO_DETAIL_SUCCESS, simulation);
    }

    @Operation(summary = "특정 시퀀스 조회", description = "특정 시퀀스 번호의 시퀀스를 조회합니다. (옵션 제외, 다음 시퀀스 존재 여부 포함)")
    @GetMapping("/simulation/{scenarioId}/sequences/{sequenceNumber}")
    public ApiResponse<SequenceResponse> getSequence(
            @Parameter(description = "시나리오 ID", example = "1")
            @PathVariable Long scenarioId,
            @Parameter(description = "시퀀스 번호 (1부터 시작)", example = "1")
            @PathVariable int sequenceNumber) {
        log.info("Fetching sequence {} for scenario ID: {}", sequenceNumber, scenarioId);
        SequenceResponse sequence = scenarioSessionService.getSequence(scenarioId, sequenceNumber);
        return ApiResponse.success(SuccessCode.SCENARIO_DETAIL_SUCCESS, sequence);
    }

    @Operation(summary = "특정 시퀀스의 옵션 조회", description = "특정 시퀀스의 모든 옵션을 조회합니다.")
    @GetMapping("/simulation/{scenarioId}/sequences/{sequenceNumber}/options")
    public ApiResponse<List<OptionResponse>> getSequenceOptions(
            @Parameter(description = "시나리오 ID", example = "1")
            @PathVariable Long scenarioId,
            @Parameter(description = "시퀀스 번호 (1부터 시작)", example = "1")
            @PathVariable int sequenceNumber) {
        log.info("Fetching options for sequence {} in scenario ID: {}", sequenceNumber, scenarioId);
        List<OptionResponse> options = scenarioSessionService.getSequenceOptions(scenarioId, sequenceNumber);
        return ApiResponse.success(SuccessCode.SCENARIO_DETAIL_SUCCESS, options);
    }
}