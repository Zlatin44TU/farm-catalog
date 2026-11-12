package com.catalog.farm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegionRequestDTO(
    @NotBlank @Size (max = 150) String name
) {}
