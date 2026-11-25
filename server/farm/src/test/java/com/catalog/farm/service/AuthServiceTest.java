package com.catalog.farm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.catalog.farm.dto.request.UserLoginDTO;
import com.catalog.farm.dto.request.UserRegisterDTO;
import com.catalog.farm.dto.response.JwtAuthResponseDTO;
import com.catalog.farm.dto.response.UserProfileResponseDTO;
import com.catalog.farm.entity.User;
import com.catalog.farm.entity.enums.UserRoles;
import com.catalog.farm.exception.ResourceAlreadyExistsException;
import com.catalog.farm.repository.FarmRepository;
import com.catalog.farm.repository.UserRepository;
import com.catalog.farm.security.jwt.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                farmRepository,
                passwordEncoder,
                jwtService,
                authenticationManager
        );
    }

    @Test
    void register_HappyPath_CreatesBuyerAndReturnsJwt() {
        UserRegisterDTO dto = new UserRegisterDTO(
                "buyer@test.com",
                "securePass123",
                "Ivan Ivanov",
                "+359888123456",
                null, // role omitted -> defaults to BUYER
                null
        );

        when(userRepository.existsByEmail("buyer@test.com")).thenReturn(false);
        when(passwordEncoder.encode("securePass123")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("buyer@test.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setFullName("Ivan Ivanov");
        savedUser.setPhoneNumber("+359888123456");
        savedUser.setRole(UserRoles.BUYER);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mock-jwt-token");

        JwtAuthResponseDTO response = authService.register(dto);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals("buyer@test.com", response.user().email());
        assertEquals(UserRoles.BUYER, response.user().role());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_AdminRole_ThrowsIllegalArgumentException() {
        UserRegisterDTO dto = new UserRegisterDTO(
                "admin@test.com",
                "adminPass123",
                "Admin User",
                "+359888999000",
                UserRoles.ADMIN,
                null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.register(dto));
        assertEquals("Cannot register as an ADMIN through public registration", ex.getMessage());
    }

    @Test
    void register_DuplicateEmail_ThrowsResourceAlreadyExistsException() {
        UserRegisterDTO dto = new UserRegisterDTO(
                "existing@test.com",
                "password123",
                "Existing User",
                "+359888111222",
                UserRoles.BUYER,
                null
        );

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> authService.register(dto));
    }

    @Test
    void login_HappyPath_ReturnsJwt() {
        UserLoginDTO dto = new UserLoginDTO("buyer@test.com", "password123");

        User user = new User();
        user.setId(10L);
        user.setEmail("buyer@test.com");
        user.setPassword("encodedPassword");
        user.setFullName("Ivan Ivanov");
        user.setPhoneNumber("+359888123456");
        user.setRole(UserRoles.BUYER);

        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mock-login-jwt");

        JwtAuthResponseDTO response = authService.login(dto);

        assertNotNull(response);
        assertEquals("mock-login-jwt", response.accessToken());
        assertEquals("buyer@test.com", response.user().email());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_InvalidCredentials_ThrowsBadCredentialsException() {
        UserLoginDTO dto = new UserLoginDTO("buyer@test.com", "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
    }

    @Test
    void getCurrentUserProfile_ReturnsProfile() {
        User user = new User();
        user.setId(5L);
        user.setEmail("me@test.com");
        user.setFullName("Me User");
        user.setPhoneNumber("+359888000111");
        user.setRole(UserRoles.FARMER);

        when(userRepository.findByEmail("me@test.com")).thenReturn(Optional.of(user));

        UserProfileResponseDTO profile = authService.getCurrentUserProfile("me@test.com");

        assertNotNull(profile);
        assertEquals(5L, profile.id());
        assertEquals("me@test.com", profile.email());
        assertEquals(UserRoles.FARMER, profile.role());
    }
}
