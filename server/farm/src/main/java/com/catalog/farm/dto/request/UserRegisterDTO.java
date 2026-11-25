package com.catalog.farm.dto.request;

import com.catalog.farm.entity.enums.UserRoles;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UserRegisterDTO(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Паролата трябва да е между 8 и 64 символа")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$", message = "Паролата трябва да съдържа поне една главна буква, една малка буква и една цифра")
    String password,

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    String fullName,

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number must be a valid format with 7 to 15 digits (e.g. +359888123456)")
    String phoneNumber,

    UserRoles role,

    @Positive(message = "Farm ID must be a positive number")
    Long farmId
) {
    public UserRoles role() {
        return role != null ? role : UserRoles.BUYER;
    }
}
