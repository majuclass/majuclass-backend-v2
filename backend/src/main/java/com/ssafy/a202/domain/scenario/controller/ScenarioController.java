package com.ssafy.a202.domain.scenario.controller;

import com.ssafy.a202.domain.scenario.dto.ScenarioResponse;
import com.ssafy.a202.domain.scenario.service.ScenarioService;
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

    @Operation(summary = "전체 시나리오 목록 조회", description = "삭제되지 않은 모든 시나리오의 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<ScenarioResponse>> getAllScenarios() {
        List<ScenarioResponse> scenarios = scenarioService.getAllScenarios();
        return ApiResponse.success(SuccessCode.SCENARIO_LIST_SUCCESS, scenarios);
    }

    @Operation(summary = "카테고리별 시나리오 목록 조회", description = "특정 카테고리의 시나리오 목록을 조회합니다.")
    @GetMapping("/category/{categoryId}")
    public ApiResponse<List<ScenarioResponse>> getScenariosByCategory(
            @Parameter(description = "카테고리 ID", example = "1")
            @PathVariable Long categoryId) {
        List<ScenarioResponse> scenarios = scenarioService.getScenariosByCategory(categoryId);
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