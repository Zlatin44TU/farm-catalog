package com.catalog.farm.service;

import org.springframework.stereotype.Service;

import com.catalog.farm.repository.RegionRepository;

import lombok.RequiredArgsConstructor;

import com.catalog.farm.dto.request.RegionRequestDTO;
import com.catalog.farm.dto.response.RegionResponseDTO;
import com.catalog.farm.entity.Region;
import com.catalog.farm.exception.ResourceAlreadyExistsException;
import com.catalog.farm.exception.ResourceNotFoundException;

import java.util.List;

@Service 
@RequiredArgsConstructor 
public class RegionService {
    private final RegionRepository regionRepository; 

    public List<RegionResponseDTO> getAllRegions() {
        return this.regionRepository.findAll()
            .stream()
            .map(RegionResponseDTO::from)
            .toList();
    } 

    public RegionResponseDTO getRegionById(Long id) {
        return regionRepository.findById(id)
            .map(RegionResponseDTO::from)
            .orElseThrow(() -> new ResourceNotFoundException("Region not found with id: " + id));
    }

    public RegionResponseDTO saveRegion(RegionRequestDTO region) {
        Region existingRegion = this.regionRepository.findOneByName(region.name());
        
        if (existingRegion != null) {
            throw new ResourceAlreadyExistsException("Region with name '" + region.name() + "' already exists.");
        }
        
        Region newRegion = new Region();
        this.apply(newRegion, region);

        return RegionResponseDTO.from(this.regionRepository.save(newRegion));
    } 

    public RegionResponseDTO updateRegion(Long id, RegionRequestDTO region) {
        Region existingRegion = this.regionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Region not found with id: " + id));

        Region regionWithSameName = this.regionRepository.findOneByName(region.name());
        if (regionWithSameName != null && !regionWithSameName.getId().equals(id)) {
            throw new ResourceAlreadyExistsException("Region with name '" + region.name() + "' already exists.");
        }

        this.apply(existingRegion, region);
        return RegionResponseDTO.from(this.regionRepository.save(existingRegion));
    } 

    public void deleteRegion(Long id) {
        Region existingRegion = this.regionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Region not found with id: " + id));
        
        this.regionRepository.delete(existingRegion);
    }

    private void apply(Region region, RegionRequestDTO req) {
        region.setName(req.name());
    }
}
