package com.catalog.farm.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "categories")
@Getter 
@Setter 
@NoArgsConstructor 
public class Category {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, length = 60, unique = true)
    private String name; 

    @Column (columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp 
	@Column (updatable = false)
	private LocalDateTime created_at;

	@UpdateTimestamp 
	private LocalDateTime updated_at;
}
