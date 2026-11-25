package com.catalog.farm.dto.request;

import java.math.BigDecimal;

import com.catalog.farm.entity.enums.InquiryStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record InquiryDTO(
        @Positive(message = "Buyer ID must be a positive number") Long buyerId,

        @NotNull(message = "Product ID is required") @Positive(message = "Product ID must be a positive number") Long productId,

        @NotNull(message = "Requested quantity is required")
        @Positive(message = "Заявеното количество трябва да е положително число")
        @DecimalMin(value = "0.01", message = "Requested quantity must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Requested quantity must have up to 8 integer digits and 2 decimals")
        BigDecimal requestedQuantity,

        @Size(max = 1000, message = "Съобщението не може да надвишава 1000 символа")
        String message,

        InquiryStatus status) {

    public InquiryDTO(Long productId, BigDecimal requestedQuantity, String message) {
        this(null, productId, requestedQuantity, message, null);
    }
}
