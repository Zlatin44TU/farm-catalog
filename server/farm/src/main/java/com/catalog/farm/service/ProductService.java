package com.catalog.farm.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catalog.farm.dto.request.BulkProductUpdateDTO.ProductItemUpdateDTO;
import com.catalog.farm.dto.request.ProductDTO;
import com.catalog.farm.dto.response.ProductResponseDTO;
import com.catalog.farm.entity.Category;
import com.catalog.farm.entity.FarmProfile;
import com.catalog.farm.entity.Product;
import com.catalog.farm.entity.User;
import com.catalog.farm.exception.ResourceNotFoundException;
import com.catalog.farm.repository.CategoryRepository;
import com.catalog.farm.repository.FarmRepository;
import com.catalog.farm.repository.ProductRepository;
import com.catalog.farm.repository.ProductSpecifications;
import com.catalog.farm.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ProductRepository productRepository;
    private final FarmRepository farmRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public Page<ProductResponseDTO> getAllProducts(
            Long categoryId,
            Long regionId,
            Integer month,
            String search,
            Pageable pageable
    ) {
        Pageable clampedPageable = clampPageable(pageable);
        Specification<Product> spec = ProductSpecifications.withFilters(categoryId, regionId, month, search);
        return this.productRepository.findAll(spec, clampedPageable).map(ProductResponseDTO::from);
    }

    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        return getAllProducts(null, null, null, null, pageable);
    }

    public ProductResponseDTO getProductById(Long id) {
        return ProductResponseDTO.from(this.findOrThrow(id));
    }

    public Page<ProductResponseDTO> getMyProducts(String userEmail, Pageable pageable) {
        User user = findUserByEmail(userEmail);
        FarmProfile farm = getUserFarm(user);
        Pageable clampedPageable = clampPageable(pageable);
        return this.productRepository.findByFarmId(farm.getId(), clampedPageable).map(ProductResponseDTO::from);
    }

    @Transactional
    public ProductResponseDTO saveProduct(ProductDTO product, String userEmail) {
        User user = findUserByEmail(userEmail);
        FarmProfile farm = getUserFarm(user);

        if (product.farmId() != null && !product.farmId().equals(farm.getId())) {
            throw new AccessDeniedException("Cannot create a product for another farm");
        }

        Category category = this.categoryRepository.findById(product.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + product.categoryId()));

        Product prod = new Product();
        prod.setFarm(farm);
        prod.setCategory(category);
        prod.setName(product.name());
        prod.setDescription(product.description());
        prod.setPrice(product.price());
        prod.setUnit(product.unit());
        prod.setSeasonMonths(product.seasonMonths());
        if (product.stockQuantity() != null) prod.setStockQuantity(product.stockQuantity());
        if (product.available() != null) prod.setAvailable(product.available());
        prod.setImgUrl(product.imgUrl());

        return ProductResponseDTO.from(this.productRepository.save(prod));
    }

    @Transactional
    public ProductResponseDTO saveProduct(ProductDTO product) {
        Product prod = new Product();
        this.apply(prod, product);
        return ProductResponseDTO.from(this.productRepository.save(prod));
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductDTO product, String userEmail) {
        Product prod = this.findOrThrow(id);
        verifyOwnership(prod, userEmail);

        Category category = this.categoryRepository.findById(product.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + product.categoryId()));

        prod.setCategory(category);
        prod.setName(product.name());
        prod.setDescription(product.description());
        prod.setPrice(product.price());
        prod.setUnit(product.unit());
        prod.setSeasonMonths(product.seasonMonths());
        if (product.stockQuantity() != null) prod.setStockQuantity(product.stockQuantity());
        if (product.available() != null) prod.setAvailable(product.available());
        prod.setImgUrl(product.imgUrl());

        return ProductResponseDTO.from(prod);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductDTO product) {
        Product prod = this.findOrThrow(id);
        this.apply(prod, product);
        return ProductResponseDTO.from(prod);
    }

    @Transactional
    public List<ProductResponseDTO> bulkUpdate(List<ProductItemUpdateDTO> items, String userEmail) {
        User user = findUserByEmail(userEmail);
        FarmProfile farm = getUserFarm(user);

        List<ProductResponseDTO> updatedProducts = new ArrayList<>();

        for (ProductItemUpdateDTO item : items) {
            Product prod = this.findOrThrow(item.id());
            if (!prod.getFarm().getId().equals(farm.getId())) {
                throw new AccessDeniedException("Product with id " + item.id() + " does not belong to your farm");
            }

            if (item.price() != null) {
                prod.setPrice(item.price());
            }
            if (item.stockQuantity() != null) {
                prod.setStockQuantity(item.stockQuantity());
            }
            if (item.isAvailable() != null) {
                prod.setAvailable(item.isAvailable());
            }

            updatedProducts.add(ProductResponseDTO.from(prod));
        }

        return updatedProducts;
    }

    @Transactional
    public void deleteProduct(Long id, String userEmail) {
        Product prod = this.findOrThrow(id);
        verifyOwnership(prod, userEmail);
        this.productRepository.delete(prod);
    }

    @Transactional
    public void deleteProduct(Long id) {
        this.productRepository.delete(this.findOrThrow(id));
    }

    private Product findOrThrow(Long id) {
        return this.productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private User findUserByEmail(String email) {
        return this.userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private FarmProfile getUserFarm(User user) {
        if (user.getFarm() == null) {
            throw new ResourceNotFoundException("Current user is not associated with any farm");
        }
        return user.getFarm();
    }

    private void verifyOwnership(Product prod, String userEmail) {
        User user = findUserByEmail(userEmail);
        FarmProfile farm = getUserFarm(user);
        if (!prod.getFarm().getId().equals(farm.getId())) {
            throw new AccessDeniedException("Product with id " + prod.getId() + " does not belong to your farm");
        }
    }

    private Pageable clampPageable(Pageable pageable) {
        int pageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        if (pageSize <= 0) {
            pageSize = 10;
        }
        return PageRequest.of(pageable.getPageNumber(), pageSize, pageable.getSort());
    }

    private void apply(Product prod, ProductDTO req) {
        FarmProfile farm = this.farmRepository.findById(req.farmId())
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found with id: " + req.farmId()));

        Category category = this.categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + req.categoryId()));

        prod.setFarm(farm);
        prod.setCategory(category);
        prod.setName(req.name());
        prod.setDescription(req.description());
        prod.setPrice(req.price());
        prod.setUnit(req.unit());
        prod.setSeasonMonths(req.seasonMonths());
        if (req.stockQuantity() != null) prod.setStockQuantity(req.stockQuantity());
        if (req.available() != null) prod.setAvailable(req.available());
        prod.setImgUrl(req.imgUrl());
    }
}
