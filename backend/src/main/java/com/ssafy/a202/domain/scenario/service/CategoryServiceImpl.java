package com.ssafy.a202.domain.scenario.service;

import com.ssafy.a202.domain.scenario.dto.response.CategoryResponse;
import com.ssafy.a202.domain.scenario.repository.ScenarioCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 카테고리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final ScenarioCategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getAllCategories() {
        log.info("모든 카테고리 목록 조회");

        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }
}