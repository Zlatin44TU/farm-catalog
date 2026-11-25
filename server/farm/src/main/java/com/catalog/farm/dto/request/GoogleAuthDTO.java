package com.catalog.farm.dto.request;

import com.catalog.farm.entity.enums.UserRoles;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthDTO(
    @NotBlank(message = "Google ID token is required")
    String idToken,

    UserRoles role
) {}
