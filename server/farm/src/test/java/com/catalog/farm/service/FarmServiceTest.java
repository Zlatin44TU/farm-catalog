package com.catalog.farm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

import com.catalog.farm.dto.request.FarmRequestDTO;
import com.catalog.farm.dto.response.FarmResponseDTO;
import com.catalog.farm.entity.FarmProfile;
import com.catalog.farm.entity.Region;
import com.catalog.farm.entity.User;
import com.catalog.farm.entity.enums.UserRoles;
import com.catalog.farm.exception.ResourceAlreadyExistsException;
import com.catalog.farm.exception.ResourceNotFoundException;
import com.catalog.farm.repository.FarmRepository;
import com.catalog.farm.repository.RegionRepository;
import com.catalog.farm.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FarmServiceTest {

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegionRepository regionRepository;

    private FarmService farmService;

    @BeforeEach
    void setUp() {
        farmService = new FarmService(farmRepository, userRepository, regionRepository);
    }

    @Test
    void createFarm_AsBuyer_Success_PromotesToFarmer() {
        String email = "buyer@test.com";
        User buyer = new User();
        buyer.setId(10L);
        buyer.setEmail(email);
        buyer.setRole(UserRoles.BUYER);
        buyer.setFarm(null);

        Region region = new Region();
        region.setId(1L);
        region.setName("Тракийска низина");

        FarmRequestDTO req = new FarmRequestDTO(
                null,
                1L,
                "Златна Тракия",
                "Био зеленчуци и плодове",
                "с. Брестовица",
                "farm@trakiya.bg",
                "https://trakiya.bg",
                "https://trakiya.bg/logo.png",
                null,
                null
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(buyer));
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));

        FarmProfile savedProfile = new FarmProfile();
        savedProfile.setId(100L);
        savedProfile.setName("Златна Тракия");
        savedProfile.setDescription("Био зеленчуци и плодове");
        savedProfile.setAddress("с. Брестовица");
        savedProfile.setContactEmail("farm@trakiya.bg");
        savedProfile.setContactWebsite("https://trakiya.bg");
        savedProfile.setImgUrl("https://trakiya.bg/logo.png");
        savedProfile.setRegion(region);
        savedProfile.setCreated_at(LocalDateTime.now());
        savedProfile.setUpdated_at(LocalDateTime.now());

        when(farmRepository.save(any(FarmProfile.class))).thenReturn(savedProfile);

        FarmResponseDTO response = farmService.createFarm(req, email);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Златна Тракия", response.name());
        assertEquals(1L, response.regionId());
        assertEquals("Тракийска низина", response.regionName());

        // Verify user role was promoted from BUYER to FARMER
        assertEquals(UserRoles.FARMER, buyer.getRole());
        assertEquals(savedProfile, buyer.getFarm());

        verify(userRepository).save(buyer);
        verify(farmRepository).save(any(FarmProfile.class));
    }

    @Test
    void createFarm_AsFarmer_Success_RoleUnchanged() {
        String email = "farmer@test.com";
        User farmer = new User();
        farmer.setId(20L);
        farmer.setEmail(email);
        farmer.setRole(UserRoles.FARMER);
        farmer.setFarm(null);

        Region region = new Region();
        region.setId(2L);
        region.setName("Родопи");

        FarmRequestDTO req = new FarmRequestDTO(
                2L,
                "Родопско Сирене",
                "Млечни продукти",
                "гр. Смолян",
                "smolyan@cheese.bg",
                null,
                null,
                null,
                null
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(farmer));
        when(regionRepository.findById(2L)).thenReturn(Optional.of(region));

        FarmProfile savedProfile = new FarmProfile();
        savedProfile.setId(200L);
        savedProfile.setName("Родопско Сирене");
        savedProfile.setRegion(region);
        savedProfile.setAddress("гр. Смолян");

        when(farmRepository.save(any(FarmProfile.class))).thenReturn(savedProfile);

        FarmResponseDTO response = farmService.createFarm(req, email);

        assertNotNull(response);
        assertEquals(200L, response.id());
        assertEquals(UserRoles.FARMER, farmer.getRole());
        assertEquals(savedProfile, farmer.getFarm());

        verify(userRepository).save(farmer);
    }

    @Test
    void createFarm_UserAlreadyHasFarm_ThrowsConflict() {
        String email = "existing@farm.com";
        User user = new User();
        user.setEmail(email);
        user.setFarm(new FarmProfile());

        FarmRequestDTO req = new FarmRequestDTO(
                1L,
                "Втора ферма",
                null,
                "Адрес",
                null,
                null,
                null,
                null,
                null
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ResourceAlreadyExistsException ex = assertThrows(
                ResourceAlreadyExistsException.class,
                () -> farmService.createFarm(req, email)
        );

        assertTrue(ex.getMessage().contains("already associated with an existing farm"));
        verify(farmRepository, never()).save(any());
    }

    @Test
    void createFarm_RegionNotFound_ThrowsNotFound() {
        String email = "farmer@test.com";
        User farmer = new User();
        farmer.setEmail(email);
        farmer.setFarm(null);

        FarmRequestDTO req = new FarmRequestDTO(
                999L,
                "Ферма Неизвестен регион",
                null,
                "Адрес",
                null,
                null,
                null,
                null,
                null
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(farmer));
        when(regionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> farmService.createFarm(req, email));
        verify(farmRepository, never()).save(any());
    }

    @Test
    void getMyFarm_Success() {
        String email = "farmer@test.com";
        Region region = new Region();
        region.setId(1L);
        region.setName("Тракия");

        FarmProfile farm = new FarmProfile();
        farm.setId(50L);
        farm.setName("Моята Ферма");
        farm.setAddress("с. Труд");
        farm.setRegion(region);

        User farmer = new User();
        farmer.setEmail(email);
        farmer.setFarm(farm);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(farmer));

        FarmResponseDTO response = farmService.getMyFarm(email);

        assertNotNull(response);
        assertEquals(50L, response.id());
        assertEquals("Моята Ферма", response.name());
    }

    @Test
    void getMyFarm_UserHasNoFarm_ThrowsNotFound() {
        String email = "buyer@test.com";
        User user = new User();
        user.setEmail(email);
        user.setFarm(null);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(ResourceNotFoundException.class, () -> farmService.getMyFarm(email));
    }

    @Test
    void updateMyFarm_Success() {
        String email = "farmer@test.com";
        Region region = new Region();
        region.setId(1L);
        region.setName("Тракия");

        FarmProfile farm = new FarmProfile();
        farm.setId(50L);
        farm.setName("Старо име");
        farm.setAddress("Стар адрес");
        farm.setRegion(region);

        User farmer = new User();
        farmer.setEmail(email);
        farmer.setFarm(farm);

        FarmRequestDTO req = new FarmRequestDTO(
                1L,
                "Ново име",
                "Ново описание",
                "Нов адрес",
                "contact@new.bg",
                null,
                null,
                null,
                null
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(farmer));
        when(farmRepository.save(any(FarmProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FarmResponseDTO updated = farmService.updateMyFarm(req, email);

        assertNotNull(updated);
        assertEquals("Ново име", updated.name());
        assertEquals("Нов адрес", updated.address());
        assertEquals("Ново описание", updated.description());
        assertEquals("contact@new.bg", updated.contactEmail());
    }

    @Test
    void updateMyFarm_UserHasNoFarm_ThrowsNotFound() {
        String email = "buyer@test.com";
        User user = new User();
        user.setEmail(email);
        user.setFarm(null);

        FarmRequestDTO req = new FarmRequestDTO(
                1L, "Име", null, "Адрес", null, null, null, null, null
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(ResourceNotFoundException.class, () -> farmService.updateMyFarm(req, email));
    }

    @Test
    void getFarmById_Success() {
        Region region = new Region();
        region.setId(1L);
        region.setName("Добруджа");

        FarmProfile farm = new FarmProfile();
        farm.setId(77L);
        farm.setName("Златна Добруджа");
        farm.setAddress("гр. Добрич");
        farm.setRegion(region);

        when(farmRepository.findById(77L)).thenReturn(Optional.of(farm));

        FarmResponseDTO response = farmService.getFarmById(77L);

        assertNotNull(response);
        assertEquals(77L, response.id());
        assertEquals("Златна Добруджа", response.name());
    }

    @Test
    void getFarmById_NotFound() {
        when(farmRepository.findById(888L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> farmService.getFarmById(888L));
    }

    @Test
    void getAllFarms_ClampsMaxPageSizeTo50() {
        Pageable requestedPageable = PageRequest.of(0, 100);

        when(farmRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        farmService.getAllFarms(null, null, requestedPageable);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(farmRepository).findAll(captor.capture());

        Pageable usedPageable = captor.getValue();
        assertEquals(50, usedPageable.getPageSize());
        assertEquals(0, usedPageable.getPageNumber());
    }

    @Test
    void getAllFarms_FiltersByRegionAndSearch() {
        Pageable requestedPageable = PageRequest.of(0, 10);

        when(farmRepository.findByRegionIdAndNameContainingIgnoreCase(eq(1L), eq("Био"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        farmService.getAllFarms(1L, "  Био  ", requestedPageable);

        verify(farmRepository).findByRegionIdAndNameContainingIgnoreCase(eq(1L), eq("Био"), any(Pageable.class));
    }
}
