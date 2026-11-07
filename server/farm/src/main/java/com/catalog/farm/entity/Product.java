package com.catalog.farm.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="products")
@Getter 
@Setter
@NoArgsConstructor
public class Product {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY) // if using Aiven DB service
	private Long id;
	
	@Column (nullable = false)
	private Long farm_id; //todo make reference to farm table
	
	@Column (nullable = false, length = 150)
	private String name;

	@Column (columnDefinition = "TEXT")
	private String description;

	@Column (nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Enumerated (EnumType.STRING)
	@Column (nullable = false)
	private Unit unit;

	@Column (nullable = false, precision = 10, scale = 2)
	private BigDecimal stock_quantity;
	
	@Column (name = "is_available", nullable = false)
	private boolean available = true;
	
	@Column (name = "img_url", length = 255)
	private String imgUrl;

	@CreationTimestamp 
	@Column (updatable = false)
	private LocalDateTime created_at;

	@UpdateTimestamp 
	private LocalDateTime updated_at; 
}