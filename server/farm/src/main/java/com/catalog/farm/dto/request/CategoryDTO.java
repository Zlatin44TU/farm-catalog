package com.catalog.farm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryDTO(
    @NotBlank(message = "Category name is required")
    @Size(max = 60, message = "Category name cannot exceed 60 characters")
    String name,

    String description
) {}
