package com.ssafy.a202.domain.scenario.service;

import com.ssafy.a202.domain.scenario.dto.response.CategoryResponse;

import java.util.List;

/**
 * 카테고리 서비스 인터페이스
 */
public interface CategoryService {

    /**
     * 모든 카테고리 목록 조회
     * @return 카테고리 목록
     */
    List<CategoryResponse> getAllCategories();
}