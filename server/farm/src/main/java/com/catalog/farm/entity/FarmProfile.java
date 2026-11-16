package com.catalog.farm.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "farm_profiles")
@Getter 
@Setter 
@NoArgsConstructor 
public class FarmProfile {
    @Id 
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private Long onwerId;

    private Long regionId;

    @Column (nullable = false, length = 150)
    private String name;

    @Column (columnDefinition = "TEXT")
    private String description; 

    @Column (nullable = false, length = 255)
    private String address;

    @Column (length = 100)
    private String contactEmail;

    @Column (length = 255)
    private String contactWebsite;

    @Column (length = 255)
    private String imgUrl; 

    @CreationTimestamp 
	@Column (updatable = false)
	private LocalDateTime created_at;

	@UpdateTimestamp 
	private LocalDateTime updated_at;
}
