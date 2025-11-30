package com.ssafy.a202.common.entity;

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
    public static <E, T> PageResponse<T> of(Page<E> page, List<T> responseList) {
        return new PageResponse<>(
                responseList,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
