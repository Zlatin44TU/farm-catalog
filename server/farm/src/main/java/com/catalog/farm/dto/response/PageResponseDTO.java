package com.catalog.farm.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record PageResponseDTO<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {
    public static <T> PageResponseDTO<T> from(Page<T> page) {
        if (page == null) {
            return null;
        }
        return new PageResponseDTO<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
    }
}
