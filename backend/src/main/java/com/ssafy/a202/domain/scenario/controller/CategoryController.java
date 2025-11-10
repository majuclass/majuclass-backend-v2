package com.ssafy.a202.domain.scenario.controller;

import com.ssafy.a202.domain.scenario.dto.response.CategoryResponse;
import com.ssafy.a202.domain.scenario.service.CategoryService;
import com.ssafy.a202.global.constants.SuccessCode;
import com.ssafy.a202.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 카테고리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "카테고리 관련 API")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회", description = "모든 카테고리 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ApiResponse.success(SuccessCode.CATEGORY_LIST_SUCCESS, categories);
    }
}