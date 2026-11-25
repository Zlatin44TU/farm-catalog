package com.catalog.farm.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catalog.farm.dto.request.InquiryDTO;
import com.catalog.farm.dto.request.InquiryStatusUpdateDTO;
import com.catalog.farm.dto.response.InquiryResponseDTO;
import com.catalog.farm.service.InquiryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@Validated
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<InquiryResponseDTO> createInquiry(
            @RequestBody @Valid InquiryDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        InquiryResponseDTO created = this.inquiryService.createInquiry(dto, userDetails.getUsername());
        URI location = URI.create(String.format("/api/inquiries/%s", created.id()));
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/sent")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<Page<InquiryResponseDTO>> getSentInquiries(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Page<InquiryResponseDTO> sent = this.inquiryService.getSentInquiries(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(sent);
    }

    @GetMapping("/received")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<Page<InquiryResponseDTO>> getReceivedInquiries(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Page<InquiryResponseDTO> received = this.inquiryService.getReceivedInquiries(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(received);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<InquiryResponseDTO> updateInquiryStatus(
            @PathVariable Long id,
            @RequestBody @Valid InquiryStatusUpdateDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        InquiryResponseDTO updated = this.inquiryService.updateInquiryStatus(id, dto.status(), userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }
}
