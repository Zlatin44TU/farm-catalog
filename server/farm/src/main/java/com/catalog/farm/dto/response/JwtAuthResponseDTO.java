package com.catalog.farm.dto.response;

public record JwtAuthResponseDTO(
    String accessToken,
    String tokenType,
    UserProfileResponseDTO user
) {
    public JwtAuthResponseDTO(String accessToken, UserProfileResponseDTO user) {
        this(accessToken, "Bearer", user);
    }
}
