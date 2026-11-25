package com.catalog.farm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
import org.springframework.security.access.AccessDeniedException;

import com.catalog.farm.dto.request.InquiryDTO;
import com.catalog.farm.dto.response.InquiryResponseDTO;
import com.catalog.farm.entity.FarmProfile;
import com.catalog.farm.entity.Inquiry;
import com.catalog.farm.entity.Product;
import com.catalog.farm.entity.User;
import com.catalog.farm.entity.enums.InquiryStatus;
import com.catalog.farm.entity.enums.Unit;
import com.catalog.farm.entity.enums.UserRoles;
import com.catalog.farm.exception.InvalidOperationException;
import com.catalog.farm.exception.ResourceNotFoundException;
import com.catalog.farm.exception.SelfInquiryNotAllowedException;
import com.catalog.farm.repository.InquiryRepository;
import com.catalog.farm.repository.ProductRepository;
import com.catalog.farm.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    private InquiryService inquiryService;

    private User buyer;
    private User farmer;
    private FarmProfile farm;
    private Product product;

    @BeforeEach
    void setUp() {
        inquiryService = new InquiryService(inquiryRepository, productRepository, userRepository);

        farm = new FarmProfile();
        farm.setId(10L);
        farm.setName("Sunny Farm");

        buyer = new User();
        buyer.setId(1L);
        buyer.setEmail("buyer@example.com");
        buyer.setFullName("Buyer One");
        buyer.setPhoneNumber("+359888111222");
        buyer.setRole(UserRoles.BUYER);

        farmer = new User();
        farmer.setId(2L);
        farmer.setEmail("farmer@example.com");
        farmer.setFullName("Farmer Bob");
        farmer.setPhoneNumber("+359888333444");
        farmer.setRole(UserRoles.FARMER);
        farmer.setFarm(farm);

        product = new Product();
        product.setId(100L);
        product.setName("Bio Apples");
        product.setUnit(Unit.KG);
        product.setFarm(farm);
    }

    @Test
    void testCreateInquiry_Success() {
        InquiryDTO dto = new InquiryDTO(100L, new BigDecimal("25.50"), "Weekly order inquiry");

        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> {
            Inquiry inq = invocation.getArgument(0);
            inq.setId(500L);
            return inq;
        });

        InquiryResponseDTO response = inquiryService.createInquiry(dto, "buyer@example.com");

        assertNotNull(response);
        assertEquals(500L, response.id());
        assertEquals(1L, response.buyerId());
        assertEquals("Buyer One", response.buyerName());
        assertEquals(100L, response.productId());
        assertEquals("Bio Apples", response.productName());
        assertEquals(10L, response.farmId());
        assertEquals("Sunny Farm", response.farmName());
        assertEquals(new BigDecimal("25.50"), response.requestedQuantity());
        assertEquals("Weekly order inquiry", response.message());
        assertEquals(InquiryStatus.PENDING, response.status());

        ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(captor.capture());
        assertEquals(InquiryStatus.PENDING, captor.getValue().getStatus());
        assertEquals(buyer, captor.getValue().getUser());
        assertEquals(product, captor.getValue().getProduct());
    }

    @Test
    void testCreateInquiry_ProductNotFound() {
        InquiryDTO dto = new InquiryDTO(999L, new BigDecimal("10.00"), "Inquiry");

        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inquiryService.createInquiry(dto, "buyer@example.com"));
        verify(inquiryRepository, never()).save(any());
    }

    @Test
    void testCreateInquiry_SelfInquiryBlocked() {
        // Buyer is also associated with the same farm
        buyer.setFarm(farm);
        InquiryDTO dto = new InquiryDTO(100L, new BigDecimal("10.00"), "Self inquiry attempt");

        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThrows(SelfInquiryNotAllowedException.class,
                () -> inquiryService.createInquiry(dto, "buyer@example.com"));
        verify(inquiryRepository, never()).save(any());
    }

    @Test
    void testGetSentInquiries_ClampsPageSizeToMax50() {
        Inquiry inquiry = new Inquiry();
        inquiry.setId(1L);
        inquiry.setUser(buyer);
        inquiry.setProduct(product);
        inquiry.setStatus(InquiryStatus.PENDING);
        inquiry.setRequestedQuantity(new BigDecimal("15.00"));

        Page<Inquiry> page = new PageImpl<>(List.of(inquiry));

        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(buyer));
        when(inquiryRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);

        // Client requests size 100
        Pageable requestedPageable = PageRequest.of(0, 100);
        Page<InquiryResponseDTO> result = inquiryService.getSentInquiries("buyer@example.com", requestedPageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(inquiryRepository).findByUserId(eq(1L), captor.capture());
        assertEquals(50, captor.getValue().getPageSize());
    }

    @Test
    void testGetReceivedInquiries_Success() {
        Inquiry inquiry = new Inquiry();
        inquiry.setId(2L);
        inquiry.setUser(buyer);
        inquiry.setProduct(product);
        inquiry.setStatus(InquiryStatus.PENDING);
        inquiry.setRequestedQuantity(new BigDecimal("20.00"));

        Page<Inquiry> page = new PageImpl<>(List.of(inquiry));

        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmer));
        when(inquiryRepository.findByProductFarmId(eq(10L), any(Pageable.class))).thenReturn(page);

        Page<InquiryResponseDTO> result = inquiryService.getReceivedInquiries("farmer@example.com",
                PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(2L, result.getContent().get(0).id());
    }

    @Test
    void testGetReceivedInquiries_FarmerHasNoFarm() {
        farmer.setFarm(null);
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmer));

        assertThrows(ResourceNotFoundException.class,
                () -> inquiryService.getReceivedInquiries("farmer@example.com", PageRequest.of(0, 10)));
    }

    @Test
    void testUpdateInquiryStatus_AcceptSuccess() {
        Inquiry inquiry = new Inquiry();
        inquiry.setId(300L);
        inquiry.setUser(buyer);
        inquiry.setProduct(product);
        inquiry.setStatus(InquiryStatus.PENDING);
        inquiry.setRequestedQuantity(new BigDecimal("50.00"));

        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmer));
        when(inquiryRepository.findById(300L)).thenReturn(Optional.of(inquiry));
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(inv -> inv.getArgument(0));

        InquiryResponseDTO response = inquiryService.updateInquiryStatus(300L, InquiryStatus.ACCEPTED,
                "farmer@example.com");

        assertNotNull(response);
        assertEquals(InquiryStatus.ACCEPTED, response.status());
        assertEquals(InquiryStatus.ACCEPTED, inquiry.getStatus());
        verify(inquiryRepository).save(inquiry);
    }

    @Test
    void testUpdateInquiryStatus_RejectSuccess() {
        Inquiry inquiry = new Inquiry();
        inquiry.setId(301L);
        inquiry.setUser(buyer);
        inquiry.setProduct(product);
        inquiry.setStatus(InquiryStatus.PENDING);
        inquiry.setRequestedQuantity(new BigDecimal("30.00"));

        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmer));
        when(inquiryRepository.findById(301L)).thenReturn(Optional.of(inquiry));
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(inv -> inv.getArgument(0));

        InquiryResponseDTO response = inquiryService.updateInquiryStatus(301L, InquiryStatus.REJECTED,
                "farmer@example.com");

        assertNotNull(response);
        assertEquals(InquiryStatus.REJECTED, response.status());
        assertEquals(InquiryStatus.REJECTED, inquiry.getStatus());
    }

    @Test
    void testUpdateInquiryStatus_AccessDenied_DifferentFarm() {
        FarmProfile anotherFarm = new FarmProfile();
        anotherFarm.setId(99L);
        product.setFarm(anotherFarm);

        Inquiry inquiry = new Inquiry();
        inquiry.setId(302L);
        inquiry.setUser(buyer);
        inquiry.setProduct(product);
        inquiry.setStatus(InquiryStatus.PENDING);

        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmer));
        when(inquiryRepository.findById(302L)).thenReturn(Optional.of(inquiry));

        assertThrows(AccessDeniedException.class,
                () -> inquiryService.updateInquiryStatus(302L, InquiryStatus.ACCEPTED, "farmer@example.com"));
        verify(inquiryRepository, never()).save(any());
    }

    @Test
    void testUpdateInquiryStatus_AccessDenied_FarmerHasNoFarm() {
        farmer.setFarm(null);
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmer));

        assertThrows(AccessDeniedException.class,
                () -> inquiryService.updateInquiryStatus(303L, InquiryStatus.ACCEPTED, "farmer@example.com"));
    }

    @Test
    void testUpdateInquiryStatus_InvalidNewStatus_PendingOrNull() {
        assertThrows(InvalidOperationException.class,
                () -> inquiryService.updateInquiryStatus(304L, InquiryStatus.PENDING, "farmer@example.com"));

        assertThrows(InvalidOperationException.class,
                () -> inquiryService.updateInquiryStatus(304L, null, "farmer@example.com"));
    }

    @Test
    void testUpdateInquiryStatus_AlreadyProcessed() {
        Inquiry inquiry = new Inquiry();
        inquiry.setId(305L);
        inquiry.setUser(buyer);
        inquiry.setProduct(product);
        inquiry.setStatus(InquiryStatus.ACCEPTED); // already accepted

        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmer));
        when(inquiryRepository.findById(305L)).thenReturn(Optional.of(inquiry));

        assertThrows(InvalidOperationException.class,
                () -> inquiryService.updateInquiryStatus(305L, InquiryStatus.REJECTED, "farmer@example.com"));
        verify(inquiryRepository, never()).save(any());
    }
}
