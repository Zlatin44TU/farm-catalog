package com.catalog.farm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.catalog.farm.entity.FarmProfile;

public interface FarmRepository 
    extends JpaRepository<FarmProfile, Long>{

}
