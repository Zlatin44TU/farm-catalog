package com.catalog.farm.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductSeasonalityDTO(
    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    Long productId,

    @NotNull(message = "Month number is required")
    @Min(value = 1, message = "Month number must be between 1 and 12")
    @Max(value = 12, message = "Month number must be between 1 and 12")
    Integer monthNumber
) {}
