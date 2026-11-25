package com.catalog.farm.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.catalog.farm.dto.request.RegionRequestDTO;
import com.catalog.farm.dto.response.ErrorDetailsDTO;
import com.catalog.farm.dto.response.RegionResponseDTO;
import com.catalog.farm.service.RegionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
@Tag(name = "Региони (/api/regions)", description = "Географски региони за локализиране на ферми и филтриране на продуктовия каталог")
public class RegionController {

    private final RegionService regionService;

    @GetMapping
    @Operation(summary = "Всички региони", description = "Взимане на всички географски региони в каталога. Публичен достъп.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Списъкът с региони е извлечен успешно",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RegionResponseDTO.class))))
    })
    public List<RegionResponseDTO> getAllRegions() {
        return this.regionService.getAllRegions();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Регион по ID", description = "Взимане на детайли за конкретен географски регион по неговото ID. Публичен достъп.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Регионът е намерен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegionResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Регионът не е намерен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public RegionResponseDTO getRegionById(
            @Parameter(description = "ID на региона", required = true, example = "1")
            @PathVariable Long id) {
        return this.regionService.getRegionById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Добавяне на нов регион", description = "Създава нов географски регион в системата. Изисква роля ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Регионът е създаден успешно",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Невалидни входни данни",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
            @ApiResponse(responseCode = "403", description = "Нямате администраторски права (ADMIN)"),
            @ApiResponse(responseCode = "409", description = "Регион с това име вече съществува",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public ResponseEntity<RegionResponseDTO> createRegion(@Valid @RequestBody RegionRequestDTO region) {
        RegionResponseDTO created = this.regionService.saveRegion(region);
        URI location = URI.create(String.format("/api/regions/%s", created.id()));
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Актуализация на регион", description = "Редактира наименованието на съществуващ регион. Изисква роля ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Регионът е актуализиран успешно",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Невалидни входни данни",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
            @ApiResponse(responseCode = "403", description = "Нямате администраторски права (ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Регионът не е намерен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
            @ApiResponse(responseCode = "409", description = "Регион с това име вече съществува",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public ResponseEntity<RegionResponseDTO> updateRegion(
            @Parameter(description = "ID на региона", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody RegionRequestDTO region) {
        RegionResponseDTO updated = this.regionService.updateRegion(id, region);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Изтриване на регион", description = "Изтрива регион от системата. Изисква роля ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Регионът е изтрит успешно"),
            @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
            @ApiResponse(responseCode = "403", description = "Нямате администраторски права (ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Регионът не е намерен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public void deleteRegion(
            @Parameter(description = "ID на региона", required = true, example = "1")
            @PathVariable Long id) {
        this.regionService.deleteRegion(id);
    }
}
