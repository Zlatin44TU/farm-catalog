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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
	
	@ManyToOne 
	@JoinColumn(name = "farm_id", nullable = false)
	private FarmProfile farm; // Reference to the farm the product belongs to
	
	@ManyToOne 
	@JoinColumn (name = "category_id", nullable = false)
	private Category category; // Reference to the category the product belongs to

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

	@ElementCollection 
	@CollectionTable (name = "product_seasonality")

	@CreationTimestamp 
	@Column (updatable = false)
	private LocalDateTime created_at;

	@UpdateTimestamp 
	private LocalDateTime updated_at; 
}