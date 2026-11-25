package com.catalog.farm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.catalog.farm.entity.Inquiry;

public interface InquiryRepository
        extends JpaRepository<Inquiry, Long> {

}
