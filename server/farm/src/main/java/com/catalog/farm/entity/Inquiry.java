package com.catalog.farm.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

import com.catalog.farm.entity.enums.InquiryStatus;

@Entity 
@Table (name = "inquiries")
@Getter 
@Setter 
@NoArgsConstructor 
public class Inquiry {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "requested_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal requestedQuantity;

    @Column (columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false, length = 15)
    private InquiryStatus status = InquiryStatus.PENDING; // Default status is PENDING

    @ManyToOne 
    @JoinColumn (name = "buyer_id", nullable = false)
    private User user; // Reference to the user who made the inquiry

    @ManyToOne 
    @JoinColumn (name = "product_id", nullable = false)
    private Product product; // Reference to the product the inquiry is about 

    @CreationTimestamp 
	@Column (name = "created_at", updatable = false)
	private LocalDateTime created_at;

	@UpdateTimestamp 
    @Column (name = "updated_at")
	private LocalDateTime updated_at;
}
