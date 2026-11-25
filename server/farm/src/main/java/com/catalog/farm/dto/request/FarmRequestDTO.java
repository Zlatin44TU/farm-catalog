package com.catalog.farm.dto.request;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record FarmRequestDTO(
    @Positive(message = "Owner ID must be a positive number")
    Long ownerId,

    @NotNull(message = "Region ID is required")
    @Positive(message = "Region ID must be a positive number")
    Long regionId,

    @NotBlank(message = "Farm name is required")
    @Size(max = 150, message = "Farm name cannot exceed 150 characters")
    String name,

    String description,

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    String address,

    @Email(message = "Contact email must be a valid email address")
    @Size(max = 100, message = "Contact email cannot exceed 100 characters")
    String contactEmail,

    @Size(max = 255, message = "Contact website cannot exceed 255 characters")
    String contactWebsite,

    @Size(max = 255, message = "Image URL cannot exceed 255 characters")
    String imgUrl,

    List<@Positive Long> members,
    List<@Positive Long> products
) {
    // Convenience constructor for backwards compatibility
    public FarmRequestDTO(
        Long regionId,
        String name,
        String description,
        String address,
        String contactEmail,
        String contactWebsite,
        String imgUrl,
        List<Long> members,
        List<Long> products
    ) {
        this(null, regionId, name, description, address, contactEmail, contactWebsite, imgUrl, members, products);
    }
}
