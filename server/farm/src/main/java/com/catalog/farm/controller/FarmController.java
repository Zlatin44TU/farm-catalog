package com.catalog.farm.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.catalog.farm.dto.request.FarmRequestDTO;
import com.catalog.farm.dto.response.ErrorDetailsDTO;
import com.catalog.farm.dto.response.FarmResponseDTO;
import com.catalog.farm.service.FarmService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/farms")
@RequiredArgsConstructor
@Tag(name = "Фермерски профили (/api/farms)", description = "Публичен каталог с ферми, детайлен преглед, създаване и актуализация на фермерски профили")
public class FarmController {

        private final FarmService farmService;

        @GetMapping
        @Operation(summary = "Преглед на списъка с всички ферми", description = "Публичен пагиниран каталог с регистрирани ферми. Поддържа филтриране по регион (regionId) и търсене по име (search). "
                        + "Размерът на страницата се ограничава служебно до максимум 50 елемента.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Успешно извлечена пагинирана страница с ферми")
        })
        public Page<FarmResponseDTO> getAllFarms(
                        @Parameter(description = "ID на регион за филтриране", example = "1") @RequestParam(required = false) Long regionId,
                        @Parameter(description = "Ключова дума за търсене по име на фермата", example = "Родопи") @RequestParam(required = false) String search,
                        @Parameter(hidden = true) Pageable pageable) {
                return farmService.getAllFarms(regionId, search, pageable);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Преглед на детайлен профил на конкретна ферма", description = "Връща детайлна информация за конкретна ферма, включително нейното местоположение (регион), адрес и контакти. Публичен достъп.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Фермата е намерена успешно", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FarmResponseDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Ферма с посоченото ID не съществува", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
        })
        public FarmResponseDTO getFarmById(
                        @Parameter(description = "ID на търсената ферма", required = true, example = "1") @PathVariable Long id) {
                return farmService.getFarmById(id);
        }

        @GetMapping("/me")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Преглед на собствения фермерски профил", description = "Връща профила на фермата, асоциирана с текущо логнатия фермер.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Профилът на фермата е извлечен успешно", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FarmResponseDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
                        @ApiResponse(responseCode = "404", description = "Потребителят няма асоциирана ферма", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
        })
        public FarmResponseDTO getMyFarm(@AuthenticationPrincipal UserDetails userDetails) {
                return farmService.getMyFarm(userDetails.getUsername());
        }

        @PostMapping
        @PreAuthorize("hasAnyRole('BUYER', 'FARMER')")
        @Operation(summary = "Създаване на профил на фермата", description = "Позволява на потребител с роля BUYER или FARMER да регистрира нова ферма (при условие че все още няма асоциирана такава). "
                        + "Ако потребителят е бил BUYER, ролята му автоматично се променя на FARMER.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Фермата е създадена успешно", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FarmResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Невалидни входни данни", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
                        @ApiResponse(responseCode = "404", description = "Посоченият регион не съществува", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
                        @ApiResponse(responseCode = "409", description = "Потребителят вече има регистрирана ферма", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
        })
        public ResponseEntity<FarmResponseDTO> createFarm(
                        @Valid @RequestBody FarmRequestDTO req,
                        @AuthenticationPrincipal UserDetails userDetails) {
                FarmResponseDTO created = farmService.createFarm(req, userDetails.getUsername());
                URI location = URI.create(String.format("/api/farms/%s", created.id()));
                return ResponseEntity.created(location).body(created);
        }

        @PutMapping("/me")
        @PreAuthorize("hasRole('FARMER')")
        @Operation(summary = "Актуализация на собствения фермерски профил", description = "Позволява на фермера да обнови данните на своята ферма (име, описание, адрес, контакти, регион).", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Фермата е актуализирана успешно", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FarmResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Невалидни входни данни", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
                        @ApiResponse(responseCode = "403", description = "Нямате роля FARMER"),
                        @ApiResponse(responseCode = "404", description = "Фермерът няма регистрирана ферма или регионът не съществува", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
        })
        public ResponseEntity<FarmResponseDTO> updateMyFarm(
                        @Valid @RequestBody FarmRequestDTO req,
                        @AuthenticationPrincipal UserDetails userDetails) {
                FarmResponseDTO updated = farmService.updateMyFarm(req, userDetails.getUsername());
                return ResponseEntity.ok(updated);
        }
}
