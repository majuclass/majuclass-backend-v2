package com.ssafy.a202.domain.scenario.controller;

import com.ssafy.a202.common.annotation.UserId;
import com.ssafy.a202.common.entity.ApiResponse;
import com.ssafy.a202.common.entity.ApiResponseEntity;
import com.ssafy.a202.common.entity.PageResponse;
import com.ssafy.a202.common.entity.SuccessCode;
import com.ssafy.a202.domain.scenario.dto.request.ScenarioRequest;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioCreateResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioDetailResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioPreviewResponse;
import com.ssafy.a202.domain.scenario.service.ScenarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scenarios")
@RequiredArgsConstructor
@Tag(name = "Scenario", description = "시나리오 관리 API")
public class ScenarioController {

    private final ScenarioService scenarioService;

    @Operation(summary = "시나리오 생성", description = "새로운 시나리오를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ScenarioCreateResponse>> createScenario(
            @UserId Long userId,
            @RequestBody ScenarioRequest request
    ) {
        ScenarioCreateResponse response = scenarioService.create(userId, request);
        return ApiResponseEntity.created(
                "/api/scenarios/" + response.scenarioId(),
                SuccessCode.SCENARIO_CREATE_SUCCESS,
                response
        );
    }

    @Operation(summary = "시나리오 목록 조회", description = "시나리오 목록을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ScenarioPreviewResponse>>> getScenarios(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        PageResponse<ScenarioPreviewResponse> response = scenarioService.getScenarios(pageable);
        return ApiResponseEntity.success(
                SuccessCode.SCENARIO_GET_LIST_SUCCESS,
                response
        );
    }

    @Operation(summary = "시나리오 상세 조회", description = "특정 시나리오의 상세 정보를 조회합니다.")
    @GetMapping("/{scenarioId}")
    public ResponseEntity<ApiResponse<ScenarioDetailResponse>> getScenario(
            @Parameter(description = "조회할 시나리오 ID", required = true, example = "1")
            @PathVariable Long scenarioId) {
        ScenarioDetailResponse response = scenarioService.getSingleScenario(scenarioId);
        return ApiResponseEntity.success(
                SuccessCode.SCENARIO_GET_DETAIL_SUCCESS,
                response
        );
    }

    @Operation(summary = "시나리오 수정", description = "기존 시나리오를 수정합니다.")
    @PutMapping("/{scenarioId}")
    public ResponseEntity<ApiResponse<Void>> updateScenario(
            @UserId Long userId,
            @Parameter(description = "수정할 시나리오 ID", required = true, example = "1")
            @PathVariable Long scenarioId,
            @RequestBody ScenarioRequest request
    ) {
        scenarioService.update(userId, scenarioId, request);
        return ApiResponseEntity.success(
                SuccessCode.SCENARIO_UPDATE_SUCCESS
        );
    }

    @Operation(summary = "시나리오 삭제", description = "시나리오를 삭제합니다.")
    @DeleteMapping("/{scenarioId}")
    public ResponseEntity<ApiResponse<Void>> deleteScenario(
            @UserId Long userId,
            @Parameter(description = "삭제할 시나리오 ID", required = true, example = "1")
            @PathVariable Long scenarioId
    ) {
        scenarioService.delete(userId, scenarioId);
        return ApiResponseEntity.success(
                SuccessCode.SCENARIO_DELETE_SUCCESS
        );
    }

}
