package com.catalog.farm.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.catalog.farm.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.catalog.farm.dto.request.ProductDTO;
import com.catalog.farm.dto.response.ProductResponseDTO;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor 
public class ProductController {
	private final ProductService productService;
	
	@GetMapping
	public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
		return this.productService.getAllProducts(pageable);
	} 

	@GetMapping("/{id}")
	public ProductResponseDTO getProductById(@PathVariable Long id) {
		return this.productService.getProductById(id);
	} 

	@PostMapping 
	public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody @Valid  ProductDTO product) {
		ProductResponseDTO created = this.productService.saveProduct(product);
		URI location = URI.create(String.format("/api/products/%s", created.id()));

		return ResponseEntity.created(location).body(created);
	} 

	@PutMapping("/{id}")
	public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id, @RequestBody @Valid ProductDTO product) {
		ProductResponseDTO updated = this.productService.updateProduct(id, product);
		return ResponseEntity.ok(updated);
	} 

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteProduct(@PathVariable Long id) {
		this.productService.deleteProduct(id);
	}
}
