package com.catalog.farm.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.catalog.farm.entity.Inquiry;
import com.catalog.farm.entity.enums.InquiryStatus;
import com.catalog.farm.entity.enums.Unit;

public record InquiryResponseDTO(
    Long id,
    Long buyerId,
    String buyerName,
    String buyerEmail,
    String buyerPhone,
    Long productId,
    String productName,
    Unit productUnit,
    Long farmId,
    String farmName,
    BigDecimal requestedQuantity,
    String message,
    InquiryStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static InquiryResponseDTO from(Inquiry inquiry) {
        if (inquiry == null) {
            return null;
        }
        Long buyerId = inquiry.getUser() != null ? inquiry.getUser().getId() : null;
        String buyerName = inquiry.getUser() != null ? inquiry.getUser().getFullName() : null;
        String buyerEmail = inquiry.getUser() != null ? inquiry.getUser().getEmail() : null;
        String buyerPhone = inquiry.getUser() != null ? inquiry.getUser().getPhoneNumber() : null;

        Long productId = inquiry.getProduct() != null ? inquiry.getProduct().getId() : null;
        String productName = inquiry.getProduct() != null ? inquiry.getProduct().getName() : null;
        Unit productUnit = inquiry.getProduct() != null ? inquiry.getProduct().getUnit() : null;

        Long farmId = (inquiry.getProduct() != null && inquiry.getProduct().getFarm() != null)
                ? inquiry.getProduct().getFarm().getId()
                : null;
        String farmName = (inquiry.getProduct() != null && inquiry.getProduct().getFarm() != null)
                ? inquiry.getProduct().getFarm().getName()
                : null;

        return new InquiryResponseDTO(
            inquiry.getId(),
            buyerId,
            buyerName,
            buyerEmail,
            buyerPhone,
            productId,
            productName,
            productUnit,
            farmId,
            farmName,
            inquiry.getRequestedQuantity(),
            inquiry.getMessage(),
            inquiry.getStatus(),
            inquiry.getCreated_at(),
            inquiry.getUpdated_at()
        );
    }
}
