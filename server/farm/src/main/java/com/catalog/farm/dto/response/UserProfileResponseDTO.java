package com.catalog.farm.dto.response;

import java.time.LocalDateTime;

import com.catalog.farm.entity.User;
import com.catalog.farm.entity.enums.UserRoles;

public record UserProfileResponseDTO(
    Long id,
    String email,
    String fullName,
    String phoneNumber,
    UserRoles role,
    Long farmId,
    String farmName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UserProfileResponseDTO from(User user) {
        if (user == null) {
            return null;
        }
        Long farmId = user.getFarm() != null ? user.getFarm().getId() : null;
        String farmName = user.getFarm() != null ? user.getFarm().getName() : null;

        return new UserProfileResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getPhoneNumber(),
            user.getRole(),
            farmId,
            farmName,
            user.getCreated_at(),
            user.getUpdated_at()
        );
    }
}
