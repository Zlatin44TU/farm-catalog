package com.catalog.farm.dto.request;

import java.math.BigDecimal;

import com.catalog.farm.entity.Unit;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductDTO (
    @NotNull Long farmId,
    @NotNull Long categoryId,
    @NotBlank @Size (max = 150) String name,
    String description,
    @NotNull  @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal price,
    @NotNull Unit unit,
    @DecimalMin ("0.00") @Digits(integer = 8, fraction = 2) BigDecimal stockQuantity,
    Boolean available,
    @Size (max = 255) String imgUrl
){}
