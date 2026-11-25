package com.catalog.farm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegionRequestDTO(
    @NotBlank(message = "Region name is required")
    @Size(max = 150, message = "Region name cannot exceed 150 characters")
    String name
) {}
