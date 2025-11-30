package com.ssafy.a202.domain.scenario.service;

import com.ssafy.a202.common.entity.PageResponse;
import com.ssafy.a202.common.roleAop.PermissionAction;
import com.ssafy.a202.common.roleAop.scenarioPermission.CheckScenarioPermission;
import com.ssafy.a202.domain.scenario.dto.request.ScenarioRequest;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioCreateResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioDetailResponse;
import com.ssafy.a202.domain.scenario.dto.response.ScenarioPreviewResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface ScenarioService {
    @Transactional
    ScenarioCreateResponse create(Long userId, ScenarioRequest request);

    PageResponse<ScenarioPreviewResponse> getScenarios(Pageable pageable);

    ScenarioDetailResponse getSingleScenario(Long scenarioId);

    @CheckScenarioPermission(PermissionAction.UPDATE)
    @Transactional
    void update(Long userId, Long scenarioId, ScenarioRequest request);

    @CheckScenarioPermission(PermissionAction.DELETE)
    @Transactional
    void delete(Long userId, Long scenarioId);
}
