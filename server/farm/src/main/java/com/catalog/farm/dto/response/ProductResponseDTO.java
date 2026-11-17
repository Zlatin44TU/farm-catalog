package com.catalog.farm.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import com.catalog.farm.entity.Product;
import com.catalog.farm.entity.enums.Unit;

public record ProductResponseDTO (
    Long id,
    Long farmId, //TODO: change to farm name
    Long categoryId, //TODO: change to category name
    String name,
    String description,
    BigDecimal price,
    Unit unit,
    BigDecimal stockQuantity,
    Set<Integer> seasonMonths,
    boolean available,
    String imgUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
){
    public static ProductResponseDTO from(Product p) {
        return new ProductResponseDTO(
            p.getId(),
            p.getFarm().getId(),
            p.getCategory().getId(),
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