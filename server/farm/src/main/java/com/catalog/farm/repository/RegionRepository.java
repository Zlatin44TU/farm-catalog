package com.catalog.farm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.catalog.farm.entity.Region;

@Repository 
public interface RegionRepository 
    extends JpaRepository<Region, Long>{

}
