package com.catalog.farm.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.catalog.farm.security.jwt.JwtService;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        // Inject 256-bit secret key and 4 hour expiration (14,400,000 ms)
        Field secretField = JwtService.class.getDeclaredField("secretKey");
        secretField.setAccessible(true);
        secretField.set(jwtService, "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");

        Field expField = JwtService.class.getDeclaredField("jwtExpiration");
        expField.setAccessible(true);
        expField.set(jwtService, 14400000L);
    }

    @Test
    void testTokenExpirationConfiguredToFourHours() {
        assertEquals(14400000L, jwtService.getExpirationTime());
    }

    @Test
    void testGenerateAndValidateToken() {
        UserDetails user = new User("test@example.com", "password", List.of(new SimpleGrantedAuthority("ROLE_BUYER")));

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void testTokenInvalidForDifferentUser() {
        UserDetails user1 = new User("user1@example.com", "password", List.of(new SimpleGrantedAuthority("ROLE_BUYER")));
        UserDetails user2 = new User("user2@example.com", "password", List.of(new SimpleGrantedAuthority("ROLE_BUYER")));

        String token = jwtService.generateToken(user1);

        assertFalse(jwtService.isTokenValid(token, user2));
    }
}
