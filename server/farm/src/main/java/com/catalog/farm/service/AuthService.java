package com.catalog.farm.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catalog.farm.dto.request.UserLoginDTO;
import com.catalog.farm.dto.request.UserRegisterDTO;
import com.catalog.farm.dto.response.JwtAuthResponseDTO;
import com.catalog.farm.dto.response.UserProfileResponseDTO;
import com.catalog.farm.entity.FarmProfile;
import com.catalog.farm.entity.User;
import com.catalog.farm.entity.enums.UserRoles;
import com.catalog.farm.exception.ResourceAlreadyExistsException;
import com.catalog.farm.exception.ResourceNotFoundException;
import com.catalog.farm.repository.FarmRepository;
import com.catalog.farm.repository.UserRepository;
import com.catalog.farm.security.jwt.JwtService;
import com.catalog.farm.security.service.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public JwtAuthResponseDTO register(UserRegisterDTO dto) {
        if (dto.role() == UserRoles.ADMIN) {
            throw new IllegalArgumentException("Cannot register as an ADMIN through public registration");
        }

        if (userRepository.existsByEmail(dto.email())) {
            throw new ResourceAlreadyExistsException("User with email '" + dto.email() + "' already exists");
        }

        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setFullName(dto.fullName());
        user.setPhoneNumber(dto.phoneNumber());
        user.setRole(dto.role() != null ? dto.role() : UserRoles.BUYER);

        if (dto.farmId() != null) {
            FarmProfile farm = farmRepository.findById(dto.farmId())
                    .orElseThrow(() -> new ResourceNotFoundException("Farm not found with id: " + dto.farmId()));
            user.setFarm(farm);
        }

        User savedUser = userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String token = jwtService.generateToken(userDetails);

        return new JwtAuthResponseDTO(token, UserProfileResponseDTO.from(savedUser));
    }

    public JwtAuthResponseDTO login(UserLoginDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + dto.email()));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        return new JwtAuthResponseDTO(token, UserProfileResponseDTO.from(user));
    }

    public UserProfileResponseDTO getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserProfileResponseDTO.from(user);
    }
}
