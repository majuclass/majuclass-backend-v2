package com.ssafy.a202.common.entity;

import com.ssafy.a202.domain.scenario.dto.response.ScenarioPreviewResponse;
import com.ssafy.a202.domain.scenario.entity.Scenario;
import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> contents,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    public static PageResponse<ScenarioPreviewResponse> of (Page<Scenario> scenarioPage, List<ScenarioPreviewResponse> responseList) {
        return new PageResponse<>(
                responseList,
                scenarioPage.getNumber(),
                scenarioPage.getSize(),
                scenarioPage.getTotalElements(),
                scenarioPage.getTotalPages(),
                scenarioPage.isFirst(),
                scenarioPage.isLast()
        );
    }
}
