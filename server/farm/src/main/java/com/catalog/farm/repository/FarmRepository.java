package com.catalog.farm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.catalog.farm.entity.FarmProfile;

public interface FarmRepository extends JpaRepository<FarmProfile, Long> {

    Page<FarmProfile> findByRegionId(Long regionId, Pageable pageable);

    Page<FarmProfile> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<FarmProfile> findByRegionIdAndNameContainingIgnoreCase(Long regionId, String name, Pageable pageable);

    boolean existsByName(String name);
}
