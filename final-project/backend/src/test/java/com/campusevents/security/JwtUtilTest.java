package com.campusevents.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil.
 * Tests token generation and validation.
 */
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    // A sufficiently long secret for testing (at least 64 characters for HS512)
    private static final String TEST_SECRET = "this-is-a-very-long-secret-key-for-testing-jwt-tokens-that-is-at-least-64-characters-long";
    private static final long TEST_EXPIRATION_MS = 3600000; // 1 hour
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", TEST_EXPIRATION_MS);
    }
    
    @Test
    @DisplayName("Should generate valid JWT token")
    void shouldGenerateValidToken() {
        // Act
        String token = jwtUtil.generateToken(1L, "test@example.com", 100L);
        
        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }
    
    @Test
    @DisplayName("Should validate token successfully")
    void shouldValidateToken() {
        // Arrange
        String token = jwtUtil.generateToken(1L, "test@example.com", 100L);
        
        // Act
        boolean isValid = jwtUtil.validateToken(token);
        
        // Assert
        assertTrue(isValid);
    }
    
    @Test
    @DisplayName("Should reject invalid token")
    void shouldRejectInvalidToken() {
        // Act
        boolean isValid = jwtUtil.validateToken("invalid.token.here");
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("Should reject empty token")
    void shouldRejectEmptyToken() {
        // Act
        boolean isValid = jwtUtil.validateToken("");
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("Should extract user ID from token")
    void shouldExtractUserId() {
        // Arrange
        String token = jwtUtil.generateToken(42L, "test@example.com", 100L);
        
        // Act
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // Assert
        assertEquals(42L, userId);
    }
    
    @Test
    @DisplayName("Should extract email from token")
    void shouldExtractEmail() {
        // Arrange
        String token = jwtUtil.generateToken(1L, "test@example.com", 100L);
        
        // Act
        String email = jwtUtil.getEmailFromToken(token);
        
        // Assert
        assertEquals("test@example.com", email);
    }
    
    @Test
    @DisplayName("Should extract campus ID from token")
    void shouldExtractCampusId() {
        // Arrange
        String token = jwtUtil.generateToken(1L, "test@example.com", 99L);
        
        // Act
        Long campusId = jwtUtil.getCampusIdFromToken(token);
        
        // Assert
        assertEquals(99L, campusId);
    }
    
    @Test
    @DisplayName("Should reject token signed with different secret")
    void shouldRejectTokenWithDifferentSecret() {
        // Arrange - generate token with different secret
        JwtUtil otherJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(otherJwtUtil, "jwtSecret", 
            "different-secret-key-for-testing-that-is-also-at-least-64-characters-long-to-be-valid");
        ReflectionTestUtils.setField(otherJwtUtil, "jwtExpirationMs", TEST_EXPIRATION_MS);
        
        String tokenFromOtherSecret = otherJwtUtil.generateToken(1L, "test@example.com", 100L);
        
        // Act
        boolean isValid = jwtUtil.validateToken(tokenFromOtherSecret);
        
        // Assert
        assertFalse(isValid);
    }
}
