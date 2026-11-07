package com.catalog.farm.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.catalog.farm.entity.Product;

@Repository
public interface ProductRepository 
	extends JpaRepository<Product, Long> {

}
