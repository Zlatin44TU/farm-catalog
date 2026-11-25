package com.catalog.farm.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catalog.farm.dto.request.InquiryDTO;
import com.catalog.farm.dto.response.InquiryResponseDTO;
import com.catalog.farm.entity.Inquiry;
import com.catalog.farm.entity.Product;
import com.catalog.farm.entity.User;
import com.catalog.farm.entity.enums.InquiryStatus;
import com.catalog.farm.exception.InvalidOperationException;
import com.catalog.farm.exception.ResourceNotFoundException;
import com.catalog.farm.exception.SelfInquiryNotAllowedException;
import com.catalog.farm.repository.InquiryRepository;
import com.catalog.farm.repository.ProductRepository;
import com.catalog.farm.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private static final int MAX_PAGE_SIZE = 50;

    private final InquiryRepository inquiryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public InquiryResponseDTO createInquiry(InquiryDTO dto, String buyerEmail) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + buyerEmail));

        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.productId()));

        if (buyer.getFarm() != null && product.getFarm() != null
                && buyer.getFarm().getId().equals(product.getFarm().getId())) {
            throw new SelfInquiryNotAllowedException("Farmers cannot send inquiries for their own products");
        }

        Inquiry inquiry = new Inquiry();
        inquiry.setUser(buyer);
        inquiry.setProduct(product);
        inquiry.setRequestedQuantity(dto.requestedQuantity());
        inquiry.setMessage(dto.message());
        inquiry.setStatus(InquiryStatus.PENDING);

        Inquiry saved = inquiryRepository.save(inquiry);
        return InquiryResponseDTO.from(saved);
    }

    public Page<InquiryResponseDTO> getSentInquiries(String buyerEmail, Pageable pageable) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + buyerEmail));

        Pageable clampedPageable = clampPageable(pageable);
        return inquiryRepository.findByUserId(buyer.getId(), clampedPageable).map(InquiryResponseDTO::from);
    }

    public Page<InquiryResponseDTO> getReceivedInquiries(String farmerEmail, Pageable pageable) {
        User farmer = userRepository.findByEmail(farmerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + farmerEmail));

        if (farmer.getFarm() == null) {
            throw new ResourceNotFoundException("Farmer does not have an associated farm profile");
        }

        Pageable clampedPageable = clampPageable(pageable);
        return inquiryRepository.findByProductFarmId(farmer.getFarm().getId(), clampedPageable)
                .map(InquiryResponseDTO::from);
    }

    @Transactional
    public InquiryResponseDTO updateInquiryStatus(Long id, InquiryStatus newStatus, String farmerEmail) {
        if (newStatus == null || newStatus == InquiryStatus.PENDING) {
            throw new InvalidOperationException("Inquiry status can only be updated to ACCEPTED or REJECTED");
        }

        User farmer = userRepository.findByEmail(farmerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + farmerEmail));

        if (farmer.getFarm() == null) {
            throw new AccessDeniedException("User does not have an associated farm profile");
        }

        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found with id: " + id));

        if (inquiry.getProduct() == null || inquiry.getProduct().getFarm() == null
                || !farmer.getFarm().getId().equals(inquiry.getProduct().getFarm().getId())) {
            throw new AccessDeniedException("You do not have permission to modify an inquiry for another farm's product");
        }

        if (inquiry.getStatus() != InquiryStatus.PENDING) {
            throw new InvalidOperationException(
                    "Only inquiries with PENDING status can be updated. Current status: " + inquiry.getStatus());
        }

        inquiry.setStatus(newStatus);
        Inquiry updated = inquiryRepository.save(inquiry);
        return InquiryResponseDTO.from(updated);
    }

    private Pageable clampPageable(Pageable pageable) {
        int pageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        if (pageSize <= 0) {
            pageSize = 10;
        }
        return PageRequest.of(pageable.getPageNumber(), pageSize, pageable.getSort());
    }
}
