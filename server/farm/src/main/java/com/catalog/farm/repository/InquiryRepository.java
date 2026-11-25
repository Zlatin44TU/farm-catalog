package com.catalog.farm.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.catalog.farm.entity.Inquiry;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    Page<Inquiry> findByUserId(Long userId, Pageable pageable);

    Page<Inquiry> findByProductFarmId(Long farmId, Pageable pageable);

    Optional<Inquiry> findByIdAndProductFarmId(Long id, Long farmId);
}
