package com.catalog.farm.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.catalog.farm.dto.request.BulkProductUpdateDTO;
import com.catalog.farm.dto.request.CategoryDTO;
import com.catalog.farm.dto.request.FarmRequestDTO;
import com.catalog.farm.dto.request.InquiryDTO;
import com.catalog.farm.dto.request.InquiryStatusUpdateDTO;
import com.catalog.farm.dto.request.ProductDTO;
import com.catalog.farm.dto.request.RegionRequestDTO;
import com.catalog.farm.dto.request.UserLoginDTO;
import com.catalog.farm.dto.request.UserRegisterDTO;
import com.catalog.farm.entity.enums.InquiryStatus;
import com.catalog.farm.entity.enums.Unit;
import com.catalog.farm.entity.enums.UserRoles;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testUserRegisterDTO_Valid() {
        UserRegisterDTO dto = new UserRegisterDTO("farmer@example.com", "password123", "Ivan Ivanov", "+359888123456", UserRoles.FARMER, null);
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void testUserRegisterDTO_InvalidEmailAndPhone() {
        UserRegisterDTO dto = new UserRegisterDTO("invalid-email", "123", "", "abc", UserRoles.BUYER, -5L);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void testUserLoginDTO_Validation() {
        UserLoginDTO valid = new UserLoginDTO("test@example.com", "secret");
        assertTrue(validator.validate(valid).isEmpty());

        UserLoginDTO invalid = new UserLoginDTO("not-an-email", "");
        assertFalse(validator.validate(invalid).isEmpty());
    }

    @Test
    void testCategoryDTO_Validation() {
        CategoryDTO valid = new CategoryDTO("Fruits", "Fresh organic fruits");
        assertTrue(validator.validate(valid).isEmpty());

        CategoryDTO invalid = new CategoryDTO("", null);
        assertFalse(validator.validate(invalid).isEmpty());
    }

    @Test
    void testRegionRequestDTO_Validation() {
        RegionRequestDTO valid = new RegionRequestDTO("Plovdiv Region");
        assertTrue(validator.validate(valid).isEmpty());

        RegionRequestDTO invalid = new RegionRequestDTO("   ");
        assertFalse(validator.validate(invalid).isEmpty());
    }

    @Test
    void testFarmRequestDTO_Validation() {
        FarmRequestDTO valid = new FarmRequestDTO(1L, "Sunny Farm", "Bio farm", "Main Street 1", "farm@example.com", "https://farm.bg", "https://farm.bg/logo.png", Collections.emptyList(), Collections.emptyList());
        assertTrue(validator.validate(valid).isEmpty());

        FarmRequestDTO invalid = new FarmRequestDTO(null, "", null, "", "bad-email", null, null, null, null);
        assertFalse(validator.validate(invalid).isEmpty());
    }

    @Test
    void testInquiryDTO_Validation() {
        InquiryDTO valid = new InquiryDTO(1L, 2L, new BigDecimal("10.50"), "Need 10kg apples", null);
        assertTrue(validator.validate(valid).isEmpty());

        InquiryDTO validOverload = new InquiryDTO(2L, new BigDecimal("10.50"), "Need 10kg apples");
        assertTrue(validator.validate(validOverload).isEmpty());

        InquiryDTO invalidNegativeQuantity = new InquiryDTO(1L, 2L, new BigDecimal("-5.00"), "Message", null);
        assertFalse(validator.validate(invalidNegativeQuantity).isEmpty());

        InquiryDTO invalidZeroQuantity = new InquiryDTO(1L, 2L, new BigDecimal("0.00"), "Message", null);
        assertFalse(validator.validate(invalidZeroQuantity).isEmpty());

        InquiryDTO invalidLongMessage = new InquiryDTO(1L, 2L, new BigDecimal("10.00"), "a".repeat(1001), null);
        assertFalse(validator.validate(invalidLongMessage).isEmpty());

        InquiryDTO invalidNullProduct = new InquiryDTO(1L, null, new BigDecimal("10.00"), "Message", null);
        assertFalse(validator.validate(invalidNullProduct).isEmpty());
    }

    @Test
    void testInquiryStatusUpdateDTO_Validation() {
        InquiryStatusUpdateDTO validAccepted = new InquiryStatusUpdateDTO(InquiryStatus.ACCEPTED);
        assertTrue(validator.validate(validAccepted).isEmpty());

        InquiryStatusUpdateDTO validRejected = new InquiryStatusUpdateDTO(InquiryStatus.REJECTED);
        assertTrue(validator.validate(validRejected).isEmpty());

        InquiryStatusUpdateDTO invalidNull = new InquiryStatusUpdateDTO(null);
        assertFalse(validator.validate(invalidNull).isEmpty());
    }

    @Test
    void testProductDTO_Validation() {
        ProductDTO valid = new ProductDTO(1L, 2L, "Apples", "Crisp red apples", new BigDecimal("2.50"), Unit.KG, new BigDecimal("100.00"), true, "http://img.jpg", Set.of(9, 10, 11));
        assertTrue(validator.validate(valid).isEmpty());

        // Price <= 0 is invalid
        ProductDTO zeroPrice = new ProductDTO(1L, 2L, "Apples", "Crisp", new BigDecimal("0.00"), Unit.KG, new BigDecimal("10.00"), true, null, Set.of(5));
        assertFalse(validator.validate(zeroPrice).isEmpty());

        // Negative price and invalid month
        ProductDTO invalidMonth = new ProductDTO(1L, 2L, "Apples", "Crisp", new BigDecimal("-1.00"), Unit.KG, new BigDecimal("10.00"), true, null, Set.of(0, 13));
        assertFalse(validator.validate(invalidMonth).isEmpty());

        // Empty seasonMonths
        ProductDTO emptyMonths = new ProductDTO(1L, 2L, "Apples", "Crisp", new BigDecimal("2.50"), Unit.KG, new BigDecimal("10.00"), true, null, Collections.emptySet());
        assertFalse(validator.validate(emptyMonths).isEmpty());

        // Name too short (< 2 chars)
        ProductDTO shortName = new ProductDTO(1L, 2L, "A", "Crisp", new BigDecimal("2.50"), Unit.KG, new BigDecimal("10.00"), true, null, Set.of(5));
        assertFalse(validator.validate(shortName).isEmpty());
    }

    @Test
    void testBulkProductUpdateDTO_Validation() {
        BulkProductUpdateDTO.ProductItemUpdateDTO item = new BulkProductUpdateDTO.ProductItemUpdateDTO(1L, new BigDecimal("4.50"), new BigDecimal("150.00"), true);
        BulkProductUpdateDTO valid = new BulkProductUpdateDTO(List.of(item));
        assertTrue(validator.validate(valid).isEmpty());

        BulkProductUpdateDTO empty = new BulkProductUpdateDTO(Collections.emptyList());
        assertFalse(validator.validate(empty).isEmpty());

        BulkProductUpdateDTO.ProductItemUpdateDTO invalidItem = new BulkProductUpdateDTO.ProductItemUpdateDTO(-1L, new BigDecimal("0.00"), new BigDecimal("-5.00"), true);
        BulkProductUpdateDTO invalid = new BulkProductUpdateDTO(List.of(invalidItem));
        assertFalse(validator.validate(invalid).isEmpty());
    }
}
