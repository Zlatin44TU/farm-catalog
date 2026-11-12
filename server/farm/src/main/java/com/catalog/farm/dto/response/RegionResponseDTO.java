package com.catalog.farm.dto.response;

import com.catalog.farm.entity.Region;

public record RegionResponseDTO(
    Long id,
    String name
) {
    public static RegionResponseDTO from(Region region) {
        return new RegionResponseDTO(
            region.getId(),
            region.getName()
        );
    }
}
