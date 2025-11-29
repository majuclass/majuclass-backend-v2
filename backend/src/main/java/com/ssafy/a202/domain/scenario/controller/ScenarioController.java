package com.ssafy.a202.domain.scenario.controller;

import com.ssafy.a202.common.annotation.UserId;
import com.ssafy.a202.common.entity.ApiResponse;
import com.ssafy.a202.common.entity.ApiResponseEntity;
import com.ssafy.a202.common.entity.PageResponse;
import com.ssafy.a202.common.entity.SuccessCode;
import com.ssafy.a202.domain.scenario.dto.request.ScenarioCreateRequest;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioCreateResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioPreviewResponse;
import com.ssafy.a202.domain.scenario.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scenarios")
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService scenarioService;

    @PostMapping
    public ResponseEntity<ApiResponse<ScenarioCreateResponse>> createScenario(
            @UserId Long userId,
            @RequestBody ScenarioCreateRequest request
    ) {
        ScenarioCreateResponse response = scenarioService.create(userId, request);
        return ApiResponseEntity.created(
                "/api/scenarios/" + response.scenarioId(),
                SuccessCode.SCENARIO_CREATE_SUCCESS,
                response
        );
    }

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

    @GetMapping("/{scenarioId}")
    public ResponseEntity<ApiResponse<ScenarioPreviewResponse>> getScenario(
            @PathVariable Long scenarioId) {
        ScenarioPreviewResponse response = scenarioService.getSingleScenario(scenarioId);
        return ApiResponseEntity.success(
                SuccessCode.SCENARIO_GET_DETAIL_SUCCESS,
                response
        );
    }
}
