package com.catalog.farm.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.catalog.farm.dto.request.FarmRequestDTO;
import com.catalog.farm.dto.response.FarmResponseDTO;
import com.catalog.farm.service.FarmService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;

    @GetMapping
    public Page<FarmResponseDTO> getAllFarms(
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return farmService.getAllFarms(regionId, search, pageable);
    }

    @GetMapping("/{id}")
    public FarmResponseDTO getFarmById(@PathVariable Long id) {
        return farmService.getFarmById(id);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public FarmResponseDTO getMyFarm(@AuthenticationPrincipal UserDetails userDetails) {
        return farmService.getMyFarm(userDetails.getUsername());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BUYER', 'FARMER')")
    public ResponseEntity<FarmResponseDTO> createFarm(
            @Valid @RequestBody FarmRequestDTO req,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        FarmResponseDTO created = farmService.createFarm(req, userDetails.getUsername());
        URI location = URI.create(String.format("/api/farms/%s", created.id()));
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<FarmResponseDTO> updateMyFarm(
            @Valid @RequestBody FarmRequestDTO req,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        FarmResponseDTO updated = farmService.updateMyFarm(req, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }
}
