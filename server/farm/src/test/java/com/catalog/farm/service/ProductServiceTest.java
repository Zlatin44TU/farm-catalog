package com.catalog.farm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import com.catalog.farm.dto.request.BulkProductUpdateDTO.ProductItemUpdateDTO;
import com.catalog.farm.dto.request.ProductDTO;
import com.catalog.farm.dto.response.ProductResponseDTO;
import com.catalog.farm.entity.Category;
import com.catalog.farm.entity.FarmProfile;
import com.catalog.farm.entity.Product;
import com.catalog.farm.entity.User;
import com.catalog.farm.entity.enums.Unit;
import com.catalog.farm.exception.ResourceNotFoundException;
import com.catalog.farm.repository.CategoryRepository;
import com.catalog.farm.repository.FarmRepository;
import com.catalog.farm.repository.ProductRepository;
import com.catalog.farm.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    private ProductService productService;

    private User farmerUser;
    private FarmProfile farm;
    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, farmRepository, categoryRepository, userRepository);

        farm = new FarmProfile();
        farm.setId(10L);
        farm.setName("Sunny Farm");

        farmerUser = new User();
        farmerUser.setId(1L);
        farmerUser.setEmail("farmer@example.com");
        farmerUser.setFarm(farm);

        category = new Category();
        category.setId(5L);
        category.setName("Apples & Pears");

        product = new Product();
        product.setId(100L);
        product.setName("Organic Gala Apples");
        product.setPrice(new BigDecimal("3.50"));
        product.setStockQuantity(new BigDecimal("50.00"));
        product.setUnit(Unit.KG);
        product.setAvailable(true);
        product.setFarm(farm);
        product.setCategory(category);
        product.setSeasonMonths(Set.of(8, 9, 10));
    }

    @Test
    void testGetAllProducts_WithFiltersAndPaginationClamping() {
        Pageable requestedPageable = PageRequest.of(0, 100);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponseDTO> result = productService.getAllProducts(5L, 2L, 9, "Gala", requestedPageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals(50, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void testGetProductById_Success() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        ProductResponseDTO res = productService.getProductById(100L);

        assertNotNull(res);
        assertEquals(100L, res.id());
        assertEquals("Organic Gala Apples", res.name());
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    void testGetMyProducts_Success() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmerUser));
        when(productRepository.findByFarmId(10L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductResponseDTO> result = productService.getMyProducts("farmer@example.com", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(100L, result.getContent().get(0).id());
    }

    @Test
    void testGetMyProducts_NoFarmThrows() {
        farmerUser.setFarm(null);
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmerUser));

        assertThrows(ResourceNotFoundException.class, () ->
                productService.getMyProducts("farmer@example.com", PageRequest.of(0, 10)));
    }

    @Test
    void testSaveProduct_Success() {
        ProductDTO dto = new ProductDTO(10L, 5L, "New Honeycrisp", "Crisp apples", new BigDecimal("4.00"),
                Unit.KG, new BigDecimal("20.00"), true, "http://img.jpg", Set.of(9, 10));

        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmerUser));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(101L);
            return p;
        });

        ProductResponseDTO result = productService.saveProduct(dto, "farmer@example.com");

        assertNotNull(result);
        assertEquals(101L, result.id());
        assertEquals("New Honeycrisp", result.name());
    }

    @Test
    void testSaveProduct_FarmMismatchThrowsForbidden() {
        ProductDTO dto = new ProductDTO(999L, 5L, "New Honeycrisp", "Crisp apples", new BigDecimal("4.00"),
                Unit.KG, new BigDecimal("20.00"), true, "http://img.jpg", Set.of(9, 10));

        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmerUser));

        assertThrows(AccessDeniedException.class, () -> productService.saveProduct(dto, "farmer@example.com"));
    }

    @Test
    void testUpdateProduct_Success() {
        ProductDTO dto = new ProductDTO(10L, 5L, "Updated Gala", "Updated desc", new BigDecimal("4.50"),
                Unit.KG, new BigDecimal("30.00"), true, "http://img.jpg", Set.of(8, 9));

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmerUser));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));

        ProductResponseDTO result = productService.updateProduct(100L, dto, "farmer@example.com");

        assertNotNull(result);
        assertEquals("Updated Gala", result.name());
        assertEquals(new BigDecimal("4.50"), result.price());
    }

    @Test
    void testUpdateProduct_OwnershipMismatchThrowsForbidden() {
        ProductDTO dto = new ProductDTO(10L, 5L, "Updated Gala", "Updated desc", new BigDecimal("4.50"),
                Unit.KG, new BigDecimal("30.00"), true, "http://img.jpg", Set.of(8, 9));

        FarmProfile otherFarm = new FarmProfile();
        otherFarm.setId(99L);
        product.setFarm(otherFarm);

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmerUser));

        assertThrows(AccessDeniedException.class, () -> productService.updateProduct(100L, dto, "farmer@example.com"));
    }

    @Test
    void testBulkUpdate_Success() {
        Product product2 = new Product();
        product2.setId(102L);
        product2.setFarm(farm);
        product2.setCategory(category);
        product2.setName("Pears");
        product2.setPrice(new BigDecimal("5.00"));
        product2.setStockQuantity(new BigDecimal("10.00"));
        product2.setAvailable(true);

        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmerUser));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(productRepository.findById(102L)).thenReturn(Optional.of(product2));

        List<ProductItemUpdateDTO> items = List.of(
                new ProductItemUpdateDTO(100L, new BigDecimal("4.50"), new BigDecimal("150.00"), true),
                new ProductItemUpdateDTO(102L, new BigDecimal("6.00"), new BigDecimal("0.00"), false)
        );

        List<ProductResponseDTO> results = productService.bulkUpdate(items, "farmer@example.com");

        assertEquals(2, results.size());
        assertEquals(new BigDecimal("4.50"), product.getPrice());
        assertEquals(new BigDecimal("150.00"), product.getStockQuantity());
        assertTrue(product.isAvailable());

        assertEquals(new BigDecimal("6.00"), product2.getPrice());
        assertEquals(new BigDecimal("0.00"), product2.getStockQuantity());
        assertFalse(product2.isAvailable());
    }

    @Test
    void testBulkUpdate_OwnershipMismatchThrows() {
        FarmProfile otherFarm = new FarmProfile();
        otherFarm.setId(88L);
        product.setFarm(otherFarm);

        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmerUser));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        List<ProductItemUpdateDTO> items = List.of(
                new ProductItemUpdateDTO(100L, new BigDecimal("4.50"), new BigDecimal("150.00"), true)
        );

        assertThrows(AccessDeniedException.class, () -> productService.bulkUpdate(items, "farmer@example.com"));
    }

    @Test
    void testDeleteProduct_Success() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmerUser));

        productService.deleteProduct(100L, "farmer@example.com");

        verify(productRepository).delete(product);
    }

    @Test
    void testDeleteProduct_OwnershipMismatchThrows() {
        FarmProfile otherFarm = new FarmProfile();
        otherFarm.setId(77L);
        product.setFarm(otherFarm);

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmerUser));

        assertThrows(AccessDeniedException.class, () -> productService.deleteProduct(100L, "farmer@example.com"));
        verify(productRepository, never()).delete(any(Product.class));
    }
}
