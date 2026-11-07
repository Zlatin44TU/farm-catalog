package com.catalog.farm.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.catalog.farm.entity.Product;
import com.catalog.farm.entity.Unit;

public record ProductResponseDTO (
    Long id,
    Long farmId,
    Long categoryId,
    String name,
    String description,
    BigDecimal price,
    Unit unit,
    BigDecimal stockQuantity,
    boolean available,
    String imgUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
){
    public static ProductResponseDTO from(Product p) {
        return new ProductResponseDTO(
            p.getId(),
            p.getFarm_id(),
            p.getCategoryId(),
            p.getName(),
            p.getDescription(),
            p.getPrice(),
            p.getUnit(),
            p.getStock_quantity(),
            p.isAvailable(),
            p.getImgUrl(),
            p.getCreated_at(),
            p.getUpdated_at()
        );
    }
}