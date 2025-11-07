package com.ssafy.a202.domain.scenario.controller;

import com.ssafy.a202.domain.scenario.dto.request.ImageUploadUrlRequest;
import com.ssafy.a202.domain.scenario.dto.request.ScenarioCreateRequest;
import com.ssafy.a202.domain.scenario.dto.request.ScenarioUpdateRequest;
import com.ssafy.a202.domain.scenario.dto.response.ImageUploadUrlResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioCreateResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioUpdateResponse;
import com.ssafy.a202.domain.scenario.service.ScenarioService;
import com.ssafy.a202.domain.scenario.dto.response.SequenceResponse;
import com.ssafy.a202.domain.scenario.dto.response.SequenceWithOptionsResponse;
import com.ssafy.a202.global.constants.SuccessCode;
import com.ssafy.a202.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @Operation(summary = "이미지 업로드 URL 생성", description = "시나리오 이미지 업로드를 위한 Presigned URL을 생성합니다.")
    @PostMapping("/image-upload-url")
    public ApiResponse<ImageUploadUrlResponse> generateImageUploadUrl(
            @RequestBody @Valid ImageUploadUrlRequest request) {
        ImageUploadUrlResponse response = scenarioService.generateImageUploadUrl(request);
        return ApiResponse.success(SuccessCode.IMAGE_UPLOAD_URL_GENERATED, response);
    }

    @Operation(summary = "시나리오 생성", description = "새로운 시나리오를 생성합니다.")
    @PostMapping("/create")
    public ApiResponse<ScenarioCreateResponse> createScenario(
            @RequestBody @Valid ScenarioCreateRequest request) {
        ScenarioCreateResponse response = scenarioService.createScenario(request);
        return ApiResponse.success(SuccessCode.SCENARIO_CREATE_SUCCESS, response);
    }

    @Operation(summary = "시나리오 목록 조회", description = "시나리오 목록을 조회합니다. 카테고리로 필터링할 수 있습니다. 썸네일 URL을 포함합니다.")
    @GetMapping
    public ApiResponse<List<ScenarioResponse>> getAllScenarios(
            @Parameter(description = "카테고리 ID (선택)", example = "1")
            @RequestParam(required = false) Long categoryId) {
        List<ScenarioResponse> scenarios = scenarioService.getAllScenarios(categoryId);
        return ApiResponse.success(SuccessCode.SCENARIO_LIST_SUCCESS, scenarios);
    }

    @Operation(summary = "시나리오 상세 조회", description = "시나리오의 상세 정보를 조회합니다. 백그라운드 이미지 URL을 포함합니다.")
    @GetMapping("/{scenarioId}")
    public ApiResponse<ScenarioResponse> getScenarioById(
            @Parameter(description = "시나리오 ID", example = "1")
            @PathVariable Long scenarioId) {
        ScenarioResponse scenario = scenarioService.getScenarioById(scenarioId);
        return ApiResponse.success(SuccessCode.SCENARIO_DETAIL_SUCCESS, scenario);
    }

    @Operation(summary = "시나리오 전체 조회 (일괄)", description = "시나리오의 모든 시퀀스와 옵션을 한번에 조회합니다. 개별 API 호출 방식 대비 성능 비교용입니다.")
    @GetMapping("/{scenarioId}/all")
    public ApiResponse<List<SequenceWithOptionsResponse>> getAllSequencesWithOptions(
            @Parameter(description = "시나리오 ID", example = "1")
            @PathVariable Long scenarioId) {
        List<SequenceWithOptionsResponse> data = scenarioService.getAllSequencesWithOptions(scenarioId);
        return ApiResponse.success(SuccessCode.SIMULATION_RETRIEVE_SUCCESS, data);
    }

    @Operation(summary = "특정 시퀀스 조회", description = "특정 시퀀스 번호의 시퀀스를 조회합니다. (옵션 제외, 다음 시퀀스 존재 여부 포함)")
    @GetMapping("/{scenarioId}/sequences/{sequenceNumber}")
    public ApiResponse<SequenceResponse> getSequence(
            @Parameter(description = "시나리오 ID", example = "1")
            @PathVariable Long scenarioId,
            @Parameter(description = "시퀀스 번호 (1부터 시작)", example = "1")
            @PathVariable int sequenceNumber) {
        SequenceResponse sequence = scenarioService.getSequence(scenarioId, sequenceNumber);
        return ApiResponse.success(SuccessCode.SEQUENCE_RETRIEVE_SUCCESS, sequence);
    }

    @Operation(summary = "특정 시퀀스의 옵션 조회", description = "특정 시퀀스의 옵션을 난이도에 따라 조회합니다. EASY는 이미지 URL 포함, NORMAL/HARD는 텍스트만 포함합니다.")
    @GetMapping("/{scenarioId}/sequences/{sequenceNumber}/options")
    public ApiResponse<List<?>> getSequenceOptions(
            @Parameter(description = "시나리오 ID", example = "1")
            @PathVariable Long scenarioId,
            @Parameter(description = "시퀀스 번호 (1부터 시작)", example = "1")
            @PathVariable int sequenceNumber,
            @Parameter(description = "난이도 (EASY, NORMAL, HARD)", example = "EASY", required = true)
            @RequestParam String difficulty) {
        List<?> options = scenarioService.getSequenceOptions(scenarioId, sequenceNumber, difficulty);
        return ApiResponse.success(SuccessCode.OPTIONS_RETRIEVE_SUCCESS, options);
    }

    @Operation(summary = "시나리오 수정", description = "시나리오의 기본 정보, 이미지, 시퀀스, 옵션을 수정합니다. 시퀀스/옵션 ID가 있으면 수정, 없으면 신규 생성, 요청에 없는 기존 데이터는 삭제됩니다.")
    @PutMapping("/{scenarioId}/update")
    public ApiResponse<ScenarioUpdateResponse> updateScenario(
            @Parameter(description = "시나리오 ID", example = "1")
            @PathVariable Long scenarioId,
            @RequestBody @Valid ScenarioUpdateRequest request) {
        ScenarioUpdateResponse response = scenarioService.updateScenario(scenarioId, request);
        return ApiResponse.success(SuccessCode.SCENARIO_UPDATE_SUCCESS, response);
    }

    @Operation(summary = "시나리오 삭제", description = "시나리오를 삭제합니다. (Soft Delete)")
    @DeleteMapping("/{scenarioId}/delete")
    public ApiResponse<Void> deleteScenario(
            @Parameter(description = "시나리오 ID", example = "1")
            @PathVariable Long scenarioId) {
        scenarioService.deleteScenario(scenarioId);
        return ApiResponse.success(SuccessCode.SCENARIO_DELETE_SUCCESS);
    }
}