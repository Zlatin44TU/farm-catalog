package com.catalog.farm.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.catalog.farm.entity.FarmProfile;
import com.catalog.farm.entity.Product;
import com.catalog.farm.entity.Region;
import com.catalog.farm.entity.User;

public record FarmResponseDTO(
    Long id,
    Region region,
    String name,
    String description,
    String address,
    String contactEmail,
    String contactWebsite,
    String imgUrl,
    List<User> members,
    List<Product> products,
    LocalDateTime created_at,
    LocalDateTime updated_at
) {
    public static FarmResponseDTO from(FarmProfile farm) {
        return new FarmResponseDTO(
            farm.getId(), 
            farm.getRegion(), 
            farm.getName(), 
            farm.getDescription(), 
            farm.getAddress(), 
            farm.getContactEmail(), 
            farm.getContactWebsite(), 
            farm.getImgUrl(), 
            farm.getMembers(), 
            farm.getProducts(), 
            farm.getCreated_at(), 
            farm.getUpdated_at()
        );
    }
}
