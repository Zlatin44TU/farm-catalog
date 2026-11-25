package com.catalog.farm.dto.response;

import java.time.LocalDateTime;

import com.catalog.farm.entity.Category;

public record CategoryResponseDTO(
    Long id,
    String name,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CategoryResponseDTO from(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponseDTO(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getCreated_at(),
            category.getUpdated_at()
        );
    }
}
