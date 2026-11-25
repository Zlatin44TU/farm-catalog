package com.catalog.farm.dto.request;

import com.catalog.farm.entity.enums.InquiryStatus;

import jakarta.validation.constraints.NotNull;

public record InquiryStatusUpdateDTO(
        @NotNull(message = "Status is required")
        InquiryStatus status) {
}
