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
import com.catalog.farm.dto.response.ProductResponseDTO;
import com.catalog.farm.service.ProductService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Page<ProductResponseDTO> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return this.productService.getAllProducts(categoryId, regionId, month, search, pageable);
    }

    @GetMapping("/my-products")
    @PreAuthorize("hasRole('FARMER')")
    public Page<ProductResponseDTO> getMyProducts(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        return this.productService.getMyProducts(userDetails.getUsername(), pageable);
    }

    @GetMapping("/{id}")
    public ProductResponseDTO getProductById(@PathVariable Long id) {
        return this.productService.getProductById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ProductResponseDTO> createProduct(
            @RequestBody @Valid ProductDTO product,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProductResponseDTO created = this.productService.saveProduct(product, userDetails.getUsername());
        URI location = URI.create(String.format("/api/products/%s", created.id()));
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/bulk")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<List<ProductResponseDTO>> bulkUpdate(
            @RequestBody @NotEmpty(message = "Items list cannot be empty") List<@Valid ProductItemUpdateDTO> items,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ProductResponseDTO> updated = this.productService.bulkUpdate(items, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid ProductDTO product,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProductResponseDTO updated = this.productService.updateProduct(id, product, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FARMER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        this.productService.deleteProduct(id, userDetails.getUsername());
    }
}
