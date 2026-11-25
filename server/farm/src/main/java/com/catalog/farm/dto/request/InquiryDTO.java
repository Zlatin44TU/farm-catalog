package com.catalog.farm.dto.request;

import java.math.BigDecimal;

import com.catalog.farm.entity.enums.Stauts;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InquiryDTO(
        @Positive(message = "Buyer ID must be a positive number") Long buyerId,

        @NotNull(message = "Product ID is required") @Positive(message = "Product ID must be a positive number") Long productId,

        @NotNull(message = "Requested quantity is required") @DecimalMin(value = "0.01", message = "Requested quantity must be greater than 0") @Digits(integer = 8, fraction = 2, message = "Requested quantity must have up to 8 integer digits and 2 decimals") BigDecimal requestedQuantity,

        String message,

        Stauts status) {
    }

