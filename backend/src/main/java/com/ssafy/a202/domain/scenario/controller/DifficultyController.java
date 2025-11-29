package com.ssafy.a202.domain.scenario.controller;

import com.ssafy.a202.common.entity.ApiResponse;
import com.ssafy.a202.common.entity.ApiResponseEntity;
import com.ssafy.a202.common.entity.SuccessCode;
import com.ssafy.a202.domain.scenario.entity.DifficultyLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/difficulty-levels")
@RequiredArgsConstructor
public class DifficultyController {

    @GetMapping
    public ResponseEntity<ApiResponse<List<DifficultyLevel>>> getDifficultyLevel() {
        List<DifficultyLevel> response = Arrays.stream(DifficultyLevel.values()).toList();
        return ApiResponseEntity.success(
                SuccessCode.DIFFICULTY_LEVEL_GET_SUCCESS,
                response
        );
    }
}
