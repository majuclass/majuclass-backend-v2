package com.ssafy.a202.domain.scenario.controller;

import com.ssafy.a202.common.annotation.UserId;
import com.ssafy.a202.common.entity.ApiResponse;
import com.ssafy.a202.common.entity.ApiResponseEntity;
import com.ssafy.a202.common.entity.SuccessCode;
import com.ssafy.a202.domain.scenario.dto.request.ScenarioCreateRequest;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioCreateResponse;
import com.ssafy.a202.domain.scenario.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
