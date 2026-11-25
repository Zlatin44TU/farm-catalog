package com.catalog.farm.controller;

import java.net.URI;
import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catalog.farm.dto.request.UserLoginDTO;
import com.catalog.farm.dto.request.UserRegisterDTO;
import com.catalog.farm.dto.response.ErrorDetailsDTO;
import com.catalog.farm.dto.response.JwtAuthResponseDTO;
import com.catalog.farm.dto.response.UserProfileResponseDTO;
import com.catalog.farm.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Автентикация (/api/auth)", description = "Регистрация, вход в системата и извличане на профила на текущо логнатия потребител")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация на нов потребител (Купувач или Фермер)",
            description = "Създава нов потребителски профил (BUYER или FARMER). Самостоятелна регистрация с роля ADMIN е забранена. "
                    + "При успешна регистрация се връща JWT токен и профил на потребителя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Успешна регистрация и генериране на JWT токен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtAuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Невалидни входни данни или опит за регистрация като ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
            @ApiResponse(responseCode = "409", description = "Потребител с този имейл вече съществува",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public ResponseEntity<JwtAuthResponseDTO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        JwtAuthResponseDTO response = authService.register(registerDTO);
        URI location = URI.create("/api/auth/me");
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в системата и генериране на JWT токен",
            description = "Автентикира потребителя с имейл и парола и генерира JWT токен с 4 часа валидност.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешна автентикация и връщане на JWT токен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtAuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Невалиден формат на входните данни",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Грешен имейл или парола (Bad Credentials)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public ResponseEntity<JwtAuthResponseDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        JwtAuthResponseDTO response = authService.login(loginDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Взимане на данните за текущо автентикирания потребител",
            description = "Връща профила на логнатия потребител според подадения Bearer JWT токен в заглавката Authorization.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данните за потребителя са извлечени успешно",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Липсващ или невалиден JWT токен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public ResponseEntity<UserProfileResponseDTO> getCurrentUser(Principal principal) {
        UserProfileResponseDTO userProfile = authService.getCurrentUserProfile(principal.getName());
        return ResponseEntity.ok(userProfile);
    }
}
