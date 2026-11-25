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

import com.catalog.farm.dto.request.CategoryDTO;
import com.catalog.farm.dto.response.CategoryResponseDTO;
import com.catalog.farm.dto.response.ErrorDetailsDTO;
import com.catalog.farm.service.CategoryService;

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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Категории (/api/categories)", description = "Номенклатура от категории за класификация на продуктите")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Всички категории", description = "Взимане на всички категории за филтър и падащи менюта. Публичен достъп.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Списъкът с категории е извлечен успешно",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CategoryResponseDTO.class))))
    })
    public List<CategoryResponseDTO> getAllCategories() {
        return this.categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Категория по ID", description = "Взимане на детайли за конкретна категория по нейното ID. Публичен достъп.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категорията е намерена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Категорията не съществува",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public CategoryResponseDTO getCategoryById(
            @Parameter(description = "ID на категорията", required = true, example = "1")
            @PathVariable Long id) {
        return this.categoryService.getCategoryById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Добавяне на нова категория", description = "Създава нова категория в номенклатурата. Изисква роля ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Категорията е създадена успешно",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Невалидни входни данни",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
            @ApiResponse(responseCode = "403", description = "Нямате администраторски права (ADMIN)"),
            @ApiResponse(responseCode = "409", description = "Категория с това име вече съществува",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryDTO category) {
        CategoryResponseDTO created = this.categoryService.createCategory(category);
        URI location = URI.create(String.format("/api/categories/%s", created.id()));
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Актуализация на категория", description = "Редакция на съществуваща категория. Изисква роля ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категорията е актуализирана успешно",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Невалидни входни данни",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
            @ApiResponse(responseCode = "403", description = "Нямате администраторски права (ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Категорията не е намерена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
            @ApiResponse(responseCode = "409", description = "Категория с това име вече съществува",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @Parameter(description = "ID на категорията", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO category) {
        CategoryResponseDTO updated = this.categoryService.updateCategory(id, category);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Изтриване на категория", description = "Изтрива категория от номенклатурата. Изисква роля ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Категорията е изтрита успешно"),
            @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
            @ApiResponse(responseCode = "403", description = "Нямате администраторски права (ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Категорията не е намерена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public void deleteCategory(
            @Parameter(description = "ID на категорията", required = true, example = "1")
            @PathVariable Long id) {
        this.categoryService.deleteCategory(id);
    }
}
