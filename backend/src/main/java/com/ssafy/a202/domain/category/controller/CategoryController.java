package com.ssafy.a202.domain.category.controller;

import com.ssafy.a202.common.entity.ApiResponse;
import com.ssafy.a202.common.entity.ApiResponseEntity;
import com.ssafy.a202.common.entity.SuccessCode;
import com.ssafy.a202.domain.category.dto.response.CategoryResponse;
import com.ssafy.a202.domain.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
@Tag(name = "Category", description = "카테고리 관리 API")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회", description = "모든 카테고리 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> responses = categoryService.getAll();
        return ApiResponseEntity.success(SuccessCode.CATEGORY_LIST_SUCCESS, responses);
    }
}
