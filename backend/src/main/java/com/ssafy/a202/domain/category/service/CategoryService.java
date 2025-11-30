package com.ssafy.a202.domain.category.service;

import com.ssafy.a202.domain.category.dto.response.CategoryResponse;
import com.ssafy.a202.domain.category.entity.Category;
import com.ssafy.a202.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAll() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> responses = new ArrayList<>();

        for (Category category : categories) {
            responses.add(CategoryResponse.of(category));
        }

        return responses;
    }
}