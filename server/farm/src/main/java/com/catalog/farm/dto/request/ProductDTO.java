package com.catalog.farm.dto.request;

import java.math.BigDecimal;
import java.util.Set;

import com.catalog.farm.entity.enums.Unit;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductDTO(
    @NotNull(message = "Farm ID is required")
    @Positive(message = "Farm ID must be a positive number")
    Long farmId,

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be a positive number")
    Long categoryId,

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name cannot exceed 150 characters")
    String name,

    String description,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Price must have up to 8 integer digits and 2 decimals")
    BigDecimal price,

    @NotNull(message = "Unit is required")
    Unit unit,

    @DecimalMin(value = "0.00", message = "Stock quantity cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Stock quantity must have up to 8 integer digits and 2 decimals")
    BigDecimal stockQuantity,

    Boolean available,

    @Size(max = 255, message = "Image URL cannot exceed 255 characters")
    String imgUrl,

    @NotNull(message = "Season months cannot be null")
    Set<@NotNull(message = "Month cannot be null") @Min(value = 1, message = "Month must be between 1 and 12") @Max(value = 12, message = "Month must be between 1 and 12") Integer> seasonMonths
) {}
