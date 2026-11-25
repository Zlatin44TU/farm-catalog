package com.catalog.farm.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BulkProductUpdateDTO(
    @NotEmpty(message = "Product update list cannot be empty")
    List<@Valid ProductItemUpdateDTO> items
) {
    public record ProductItemUpdateDTO(
        @JsonAlias({"id", "productId"})
        @NotNull(message = "Product ID is required")
        @Positive(message = "Product ID must be positive")
        Long id,

        @DecimalMin(value = "0.01", message = "Price must be greater than 0.00")
        @Digits(integer = 8, fraction = 2, message = "Price must have up to 8 integer digits and 2 decimals")
        BigDecimal price,

        @DecimalMin(value = "0.00", message = "Stock quantity cannot be negative")
        @Digits(integer = 8, fraction = 2, message = "Stock quantity must have up to 8 integer digits and 2 decimals")
        BigDecimal stockQuantity,

        @JsonAlias({"isAvailable", "available"})
        Boolean isAvailable
    ) {}
}
