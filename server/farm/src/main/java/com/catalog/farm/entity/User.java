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
@Table(name="users")
@Getter 
@Setter 
@NoArgsConstructor
public class User {
    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique = true, length = 100)
    private String email; 

    @Column(nullable=false, length = 255)
    private String password; 

    @Column(nullable = false, length = 150)
    private String fullName; 

    @Column(nullable = false, length = 20)
    private String phoneNumber; 

    @Column(columnDefinition = "NOT NULL DEFAULT 'BUYER'")
    private UserRoles role = UserRoles.BUYER; // default role is BUYER

    @Column(nullable = false)
    private Long farmId; 

    @CreationTimestamp 
	@Column (updatable = false)
	private LocalDateTime created_at;

	@UpdateTimestamp 
	private LocalDateTime updated_at;
}
