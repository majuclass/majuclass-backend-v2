package com.ssafy.a202.domain.scenario.controller;

import com.ssafy.a202.domain.scenario.dto.ScenarioResponse;
import com.ssafy.a202.domain.scenario.service.ScenarioService;
import com.ssafy.a202.global.constants.Difficulty;
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
 * 시나리오 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/scenarios")
@RequiredArgsConstructor
@Tag(name = "Scenario", description = "시나리오 관련 API")
public class ScenarioController {

    private final ScenarioService scenarioService;

    @Operation(summary = "시나리오 목록 조회", description = "시나리오 목록을 조회합니다. 카테고리와 난이도로 필터링할 수 있습니다.")
    @GetMapping
    public ApiResponse<List<ScenarioResponse>> getAllScenarios(
            @Parameter(description = "카테고리 ID (선택)", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "난이도 (선택, EASY 또는 HARD)", example = "EASY")
            @RequestParam(required = false) Difficulty difficulty) {
        List<ScenarioResponse> scenarios = scenarioService.getAllScenarios(categoryId, difficulty);
        return ApiResponse.success(SuccessCode.SCENARIO_LIST_SUCCESS, scenarios);
    }

    @Operation(summary = "시나리오 상세 조회", description = "시나리오의 상세 정보를 조회합니다.")
    @GetMapping("/{scenarioId}")
    public ApiResponse<ScenarioResponse> getScenarioById(
            @Parameter(description = "시나리오 ID", example = "1")
            @PathVariable Long scenarioId) {
        ScenarioResponse scenario = scenarioService.getScenarioById(scenarioId);
        return ApiResponse.success(SuccessCode.SCENARIO_DETAIL_SUCCESS, scenario);
    }
}