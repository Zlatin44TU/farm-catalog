package com.catalog.farm.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne 
    // @JoinColumn (name = "owner_id", nullable = false)
    // private User owner; // not needed for now, not logic implemented

    @ManyToOne 
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column (name = "farm_name", nullable = false, length = 150)
    private String name;

    @Column (columnDefinition = "TEXT")
    private String description; 

    @Column (nullable = false, length = 255)
    private String address;

    @Column (length = 100)
    private String contactEmail;

    @Column (name = "contact_website", length = 255)
    private String contactWebsite;

    @Column (name = "img_url", length = 255)
    private String imgUrl; 

    @OneToMany(mappedBy = "farm")
    private List<User> members = new ArrayList<>(); 

    @OneToMany(mappedBy = "farm")
    private List<Product> products;

    @CreationTimestamp 
	@Column (name = "created_at", updatable = false)
	private LocalDateTime created_at;

	@UpdateTimestamp 
    @Column (name = "updated_at")
	private LocalDateTime updated_at;
}
