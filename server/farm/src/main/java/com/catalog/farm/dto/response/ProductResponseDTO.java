package com.catalog.farm.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import com.catalog.farm.entity.Product;
import com.catalog.farm.entity.enums.Unit;

public record ProductResponseDTO(
        Long id,
        Long farmId,
        String farmName,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        BigDecimal price,
        Unit unit,
        BigDecimal stockQuantity,
        Set<Integer> seasonMonths,
        boolean available,
        String imgUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    // Backward-compatible constructor
    public ProductResponseDTO(
            Long id,
            Long farmId,
            Long categoryId,
            String name,
            String description,
            BigDecimal price,
            Unit unit,
            BigDecimal stockQuantity,
            Set<Integer> seasonMonths,
            boolean available,
            String imgUrl,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this(id, farmId, null, categoryId, null, name, description, price, unit, stockQuantity, seasonMonths, available,
                imgUrl, createdAt, updatedAt);
    }

    public static ProductResponseDTO from(Product p) {
        if (p == null) {
            return null;
        }
        Long farmId = p.getFarm() != null ? p.getFarm().getId() : null;
        String farmName = p.getFarm() != null ? p.getFarm().getName() : null;
        Long categoryId = p.getCategory() != null ? p.getCategory().getId() : null;
        String categoryName = p.getCategory() != null ? p.getCategory().getName() : null;

        return new ProductResponseDTO(
            p.getId(),
            farmId,
            farmName,
            categoryId,
            categoryName,
            p.getName(),
            p.getDescription(),
            p.getPrice(),
            p.getUnit(),
            p.getStockQuantity(),
            p.getSeasonMonths(),
            p.isAvailable(),
            p.getImgUrl(),
            p.getCreated_at(),
            p.getUpdated_at()
        );
    }
}