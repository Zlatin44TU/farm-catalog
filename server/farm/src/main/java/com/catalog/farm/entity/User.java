package com.catalog.farm.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.catalog.farm.entity.enums.UserRoles;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @JsonIgnore
    @Column(nullable=false, length = 255)
    private String password; 

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName; 

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRoles role = UserRoles.BUYER; // default role is BUYER

    @ManyToOne
    @JoinColumn(name = "farm_id")
    private FarmProfile farm; // Reference to the farm the user is associated with

    @CreationTimestamp 
	@Column (name = "created_at", updatable = false)
	private LocalDateTime created_at;

	@UpdateTimestamp 
    @Column (name = "updated_at")
	private LocalDateTime updated_at;
}
