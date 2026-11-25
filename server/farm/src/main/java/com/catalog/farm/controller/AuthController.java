package com.catalog.farm.controller;

import java.net.URI;
import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catalog.farm.dto.request.UserLoginDTO;
import com.catalog.farm.dto.request.UserRegisterDTO;
import com.catalog.farm.dto.response.JwtAuthResponseDTO;
import com.catalog.farm.dto.response.UserProfileResponseDTO;
import com.catalog.farm.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponseDTO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        JwtAuthResponseDTO response = authService.register(registerDTO);
        URI location = URI.create("/api/auth/me");
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponseDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        JwtAuthResponseDTO response = authService.login(loginDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getCurrentUser(Principal principal) {
        UserProfileResponseDTO userProfile = authService.getCurrentUserProfile(principal.getName());
        return ResponseEntity.ok(userProfile);
    }
}
