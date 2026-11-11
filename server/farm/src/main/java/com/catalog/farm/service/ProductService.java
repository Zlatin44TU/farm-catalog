package com.catalog.farm.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catalog.farm.repository.ProductRepository;
import com.catalog.farm.dto.response.ProductResponseDTO;
import com.catalog.farm.dto.request.ProductDTO;

import lombok.RequiredArgsConstructor;

import com.catalog.farm.entity.Product;
import com.catalog.farm.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor 
@Transactional (readOnly = true)
public class ProductService {
	private final ProductRepository productRepository;
	
	public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
		return this.productRepository.findAll(pageable)
		.map(ProductResponseDTO::from);
	} 
	
	public ProductResponseDTO getProductById(Long id) {
		return ProductResponseDTO.from(this.findOrThrow(id));
	} 

	@Transactional
	public ProductResponseDTO saveProduct(ProductDTO product) {
		Product prod = new Product();
		this.apply(prod, product);
		return ProductResponseDTO.from(this.productRepository.save(prod));
	} 

	@Transactional
	public ProductResponseDTO updateProduct(Long id, ProductDTO product) {
		Product prod = this.findOrThrow(id);
		this.apply(prod, product); // managed entity, so changes are saved on commit
		return ProductResponseDTO.from(prod);
	}	

	@Transactional
	public void deleteProduct(Long id) { //TODO: make soft delete
		this.productRepository.delete(this.findOrThrow(id));
	} 

	private Product findOrThrow(Long id) {
		return this.productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
	} 

	private void apply(Product prod, ProductDTO req) {
		prod.setFarm_id(req.farmId());
		prod.setCategoryId(req.categoryId());
		prod.setName(req.name());
		prod.setDescription(req.description());
		prod.setPrice(req.price());
		prod.setUnit(req.unit());
		if (req.stockQuantity() != null) prod.setStock_quantity(req.stockQuantity());
		if (req.available() != null) prod.setAvailable(req.available());
		prod.setImgUrl(req.imgUrl());
	}
}
