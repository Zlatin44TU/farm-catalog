package com.catalog.farm.service;

import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catalog.farm.dto.request.FarmRequestDTO;
import com.catalog.farm.dto.response.FarmResponseDTO;
import com.catalog.farm.entity.FarmProfile;
import com.catalog.farm.entity.Region;
import com.catalog.farm.entity.User;
import com.catalog.farm.entity.enums.UserRoles;
import com.catalog.farm.exception.ResourceAlreadyExistsException;
import com.catalog.farm.exception.ResourceNotFoundException;
import com.catalog.farm.repository.FarmRepository;
import com.catalog.farm.repository.RegionRepository;
import com.catalog.farm.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FarmService {

    private static final int MAX_PAGE_SIZE = 50;

    private final FarmRepository farmRepository;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;

    public Page<FarmResponseDTO> getAllFarms(Long regionId, String search, Pageable pageable) {
        int pageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        if (pageSize <= 0) {
            pageSize = 10;
        }
        Pageable clampedPageable = PageRequest.of(pageable.getPageNumber(), pageSize, pageable.getSort());

        Page<FarmProfile> page;
        boolean hasSearch = search != null && !search.isBlank();

        if (regionId != null && hasSearch) {
            page = farmRepository.findByRegionIdAndNameContainingIgnoreCase(regionId, search.trim(), clampedPageable);
        } else if (regionId != null) {
            page = farmRepository.findByRegionId(regionId, clampedPageable);
        } else if (hasSearch) {
            page = farmRepository.findByNameContainingIgnoreCase(search.trim(), clampedPageable);
        } else {
            page = farmRepository.findAll(clampedPageable);
        }

        return page.map(FarmResponseDTO::from);
    }

    public FarmResponseDTO getFarmById(Long id) {
        FarmProfile farm = farmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found with id: " + id));
        return FarmResponseDTO.from(farm);
    }

    public FarmResponseDTO getMyFarm(String email) {
        User user = findUserByEmail(email);
        if (user.getFarm() == null) {
            throw new ResourceNotFoundException("Current user is not associated with any farm");
        }
        return FarmResponseDTO.from(user.getFarm());
    }

    @Transactional
    public FarmResponseDTO createFarm(FarmRequestDTO req, String email) {
        User user = findUserByEmail(email);

        if (user.getFarm() != null) {
            throw new ResourceAlreadyExistsException("User is already associated with an existing farm");
        }

        Region region = regionRepository.findById(req.regionId())
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with id: " + req.regionId()));

        FarmProfile farm = new FarmProfile();
        farm.setName(req.name());
        farm.setDescription(req.description());
        farm.setAddress(req.address());
        farm.setContactEmail(req.contactEmail());
        farm.setContactWebsite(req.contactWebsite());
        farm.setImgUrl(req.imgUrl());
        farm.setRegion(region);

        FarmProfile savedFarm = farmRepository.save(farm);

        // Associate user with the new farm
        user.setFarm(savedFarm);

        // Elevate BUYER role to FARMER upon farm creation
        if (user.getRole() == UserRoles.BUYER) {
            user.setRole(UserRoles.FARMER);
        }
        userRepository.save(user);

        // Include user in farm members for response mapping
        if (savedFarm.getMembers() == null) {
            savedFarm.setMembers(new ArrayList<>());
        }
        savedFarm.getMembers().add(user);

        return FarmResponseDTO.from(savedFarm);
    }

    @Transactional
    public FarmResponseDTO updateMyFarm(FarmRequestDTO req, String email) {
        User user = findUserByEmail(email);
        FarmProfile farm = user.getFarm();

        if (farm == null) {
            throw new ResourceNotFoundException("Current user has no associated farm profile to update");
        }

        if (req.regionId() != null && (farm.getRegion() == null || !req.regionId().equals(farm.getRegion().getId()))) {
            Region region = regionRepository.findById(req.regionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Region not found with id: " + req.regionId()));
            farm.setRegion(region);
        }

        farm.setName(req.name());
        farm.setDescription(req.description());
        farm.setAddress(req.address());
        farm.setContactEmail(req.contactEmail());
        farm.setContactWebsite(req.contactWebsite());
        farm.setImgUrl(req.imgUrl());

        FarmProfile updatedFarm = farmRepository.save(farm);
        return FarmResponseDTO.from(updatedFarm);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
