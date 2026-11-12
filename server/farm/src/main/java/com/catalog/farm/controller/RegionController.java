package com.catalog.farm.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.catalog.farm.service.RegionService;

import lombok.RequiredArgsConstructor;

import com.catalog.farm.dto.request.RegionRequestDTO;
import com.catalog.farm.dto.response.RegionResponseDTO;

@RestController
@RequestMapping("/api/regions") 
@RequiredArgsConstructor
public class RegionController {
    private final RegionService regionService; 

    @GetMapping 
    public List<RegionResponseDTO> getAllRegions() {
        return this.regionService.getAllRegions();
    } 

    @GetMapping("/{id}")
    public RegionResponseDTO getRegionById(@PathVariable Long id) {
        return this.regionService.getRegionById(id);
    } 

    @PostMapping 
    public ResponseEntity<RegionResponseDTO> createRegion(@RequestBody RegionRequestDTO region) {
        RegionResponseDTO created =  this.regionService.saveRegion(region); 
        URI location = URI.create(String.format("/api/regions/%s", created.id()));

        return ResponseEntity.created(location).body(created);
    } 

    @PutMapping("/{id}")
    public ResponseEntity<RegionResponseDTO> updateRegion(@PathVariable Long id, @RequestBody RegionRequestDTO region) {
        RegionResponseDTO updated = this.regionService.updateRegion(id, region);
        return ResponseEntity.ok(updated);
    } 

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRegion(@PathVariable Long id) {
        this.regionService.deleteRegion(id);
    }
}
