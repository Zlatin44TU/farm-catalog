package com.catalog.farm.dto.response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.catalog.farm.entity.FarmProfile;
import com.catalog.farm.entity.User;

public record FarmResponseDTO(
    Long id,
    Long regionId,
    String regionName,
    String name,
    String description,
    String address,
    String contactEmail,
    String contactWebsite,
    String imgUrl,
    List<Long> memberIds,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static FarmResponseDTO from(FarmProfile farm) {
        if (farm == null) {
            return null;
        }
        Long regionId = farm.getRegion() != null ? farm.getRegion().getId() : null;
        String regionName = farm.getRegion() != null ? farm.getRegion().getName() : null;

        List<Long> memberIds = farm.getMembers() != null
            ? farm.getMembers().stream().map(User::getId).toList()
            : Collections.emptyList();

        return new FarmResponseDTO(
            farm.getId(),
            regionId,
            regionName,
            farm.getName(),
            farm.getDescription(),
            farm.getAddress(),
            farm.getContactEmail(),
            farm.getContactWebsite(),
            farm.getImgUrl(),
            memberIds,
            farm.getCreated_at(),
            farm.getUpdated_at()
        );
    }
}
