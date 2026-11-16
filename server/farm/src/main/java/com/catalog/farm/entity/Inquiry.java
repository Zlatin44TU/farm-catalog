package com.catalog.farm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "inquiries")
@Getter 
@Setter 
@NoArgsConstructor 
public class Inquiry {

    @ManyToOne 
    private User user; // Reference to the user who made the inquiry

    @ManyToOne 
    private Product product; // Reference to the product the inquiry is about
}
