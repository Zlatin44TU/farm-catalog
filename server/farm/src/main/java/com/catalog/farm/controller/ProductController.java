package com.catalog.farm.controller;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.catalog.farm.dto.request.BulkProductUpdateDTO.ProductItemUpdateDTO;
import com.catalog.farm.dto.request.ProductDTO;
import com.catalog.farm.dto.response.ErrorDetailsDTO;
import com.catalog.farm.dto.response.ProductResponseDTO;
import com.catalog.farm.service.ProductService;

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
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Продукти (/api/products)", description = "Каталог на продукти, филтрация по категория, регион и месец на сезонност, детайли и масова актуализация")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Каталог с продукти",
            description = "Публичен продуктов каталог с филтри: categoryId, regionId, month (1-12 за сезонност), search (по име и описание). "
                    + "Поддържа пагинация (page, size). Размерът на страницата е защитен до максимум 50 елемента.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно извлечена пагинирана страница с продукти")
    })
    public Page<ProductResponseDTO> getAllProducts(
            @Parameter(description = "ID на категория за филтриране", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "ID на регион на фермата производител", example = "2")
            @RequestParam(required = false) Long regionId,
            @Parameter(description = "Календарен месец за сезонност (1 = Януари ... 12 = Декември)", example = "9")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Търсене по ключова дума в име или описание", example = "ябълки")
            @RequestParam(required = false) String search,
            @Parameter(hidden = true) Pageable pageable) {
        return this.productService.getAllProducts(categoryId, regionId, month, search, pageable);
    }

    @GetMapping("/my-products")
    @PreAuthorize("hasRole('FARMER')")
    @Operation(summary = "Взимане на продуктите само на логнатия фермер",
            description = "Връща пагиниран списък с всички продукти на фермата, управлявана от логнатия фермер (за Dashboard).",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Продуктите на фермера са извлечени успешно"),
            @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
            @ApiResponse(responseCode = "403", description = "Нямате роля FARMER"),
            @ApiResponse(responseCode = "404", description = "Фермерът няма регистрирана ферма",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public Page<ProductResponseDTO> getMyProducts(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(hidden = true) Pageable pageable) {
        return this.productService.getMyProducts(userDetails.getUsername(), pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Преглед на детайлна страница на продукт",
            description = "Връща пълна информация за конкретен продукт, включително категория, данни за фермата производител и месеци на сезонност.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Продуктът е намерен успешно",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Продукт с посоченото ID не съществува",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public ProductResponseDTO getProductById(
            @Parameter(description = "ID на продукта", required = true, example = "101")
            @PathVariable Long id) {
        return this.productService.getProductById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    @Operation(summary = "Създаване на нов продукт",
            description = "Добавя нов продукт към фермата на логнатия фермер. Изисква посочване на категория, име, цена, мерна единица, наличност и списък с месеци на сезонност (1-12).",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Продуктът е създаден успешно",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Невалидни входни данни",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
            @ApiResponse(responseCode = "403", description = "Нямате роля FARMER или опитвате да запишете за чужда ферма"),
            @ApiResponse(responseCode = "404", description = "Категорията или фермата не съществуват",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public ResponseEntity<ProductResponseDTO> createProduct(
            @RequestBody @Valid ProductDTO product,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProductResponseDTO created = this.productService.saveProduct(product, userDetails.getUsername());
        URI location = URI.create(String.format("/api/products/%s", created.id()));
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/bulk")
    @PreAuthorize("hasRole('FARMER')")
    @Operation(summary = "Масова актуализация на цени и наличности (Bulk Edit)",
            description = "Изпълнява атомарна актуализация на списък от продукти в единна транзакция. "
                    + "Позволява бърза промяна на цени (price), наличности (stockQuantity) и статус на наличност (isAvailable). "
                    + "Ако някой продукт не принадлежи на логнатия фермер, цялата транзакция се връща назад (Rollback).",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Всички продукти са актуализирани успешно",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProductResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Невалидни входни данни или празен списък",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
            @ApiResponse(responseCode = "403", description = "Някой от продуктите принадлежи на друга ферма"),
            @ApiResponse(responseCode = "404", description = "Някой от посочените продукти не е намерен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public ResponseEntity<List<ProductResponseDTO>> bulkUpdate(
            @RequestBody @NotEmpty(message = "Items list cannot be empty") List<@Valid ProductItemUpdateDTO> items,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ProductResponseDTO> updated = this.productService.bulkUpdate(items, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FARMER')")
    @Operation(summary = "Пълна редакция на съществуващ продукт",
            description = "Редакция на параметрите на продукт. Продуктът трябва задължително да принадлежи на фермата на логнатия фермер.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Продуктът е актуализиран успешно",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Невалидни входни данни",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
            @ApiResponse(responseCode = "403", description = "Нямате право да редактирате продукт на друга ферма"),
            @ApiResponse(responseCode = "404", description = "Продуктът не е намерен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @Parameter(description = "ID на продукта за редакция", required = true, example = "101")
            @PathVariable Long id,
            @RequestBody @Valid ProductDTO product,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProductResponseDTO updated = this.productService.updateProduct(id, product, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FARMER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Изтриване на продукт",
            description = "Изтрива продукт от каталога. Достъпно само за фермера собственик на фермата, към която принадлежи продуктът.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Продуктът е изтрит успешно"),
            @ApiResponse(responseCode = "401", description = "Неавтентикиран потребител"),
            @ApiResponse(responseCode = "403", description = "Нямате право да изтривате продукт на друга ферма"),
            @ApiResponse(responseCode = "404", description = "Продуктът не е намерен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsDTO.class)))
    })
    public void deleteProduct(
            @Parameter(description = "ID на продукта за изтриване", required = true, example = "101")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        this.productService.deleteProduct(id, userDetails.getUsername());
    }
}
