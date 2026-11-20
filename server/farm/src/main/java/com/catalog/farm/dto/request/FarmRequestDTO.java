package com.catalog.farm.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FarmRequestDTO(
    @NotNull Long regionId,
    @NotNull String name,
    String description,
    @NotNull @Size (max = 255) String address,
    @Size (max = 100) String contactEmail,
    @Size (max = 255) String contactWebsite,
    @Size (max = 255) String imgUrl,
    List<Long> members,
    List<Long> products
) {}
