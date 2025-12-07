package com.campusevents.service;

import com.campusevents.dto.AuthResponse;
import com.campusevents.dto.LoginRequest;
import com.campusevents.dto.SignupRequest;
import com.campusevents.model.User;
import com.campusevents.repository.UserRepository;
import com.campusevents.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests signup and login functionality with mocked dependencies.
 */
class AuthServiceTest {
    
    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        // Use mock() instead of @Mock annotation for Java 23 compatibility
        userRepository = mock(UserRepository.class);
        jwtUtil = mock(JwtUtil.class);
        authService = new AuthService(userRepository, jwtUtil);
    }
    
    @Nested
    @DisplayName("Signup Tests")
    class SignupTests {
        
        @Test
        @DisplayName("Should signup successfully with valid data")
        void shouldSignupSuccessfully() {
            // Arrange
            SignupRequest request = new SignupRequest(
                "John", "Doe", "john@example.com", "password123", 1L
            );
            
            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });
            when(jwtUtil.generateToken(1L, "john@example.com", 1L)).thenReturn("test-jwt-token");
            
            // Act
            AuthResponse response = authService.signup(request);
            
            // Assert
            assertNotNull(response);
            assertEquals("test-jwt-token", response.getToken());
            assertEquals(1L, response.getUserId());
            assertEquals("john@example.com", response.getEmail());
            assertEquals("John", response.getFirstName());
            assertEquals("Doe", response.getLastName());
            assertEquals(1L, response.getCampusId());
            
            verify(userRepository).existsByEmail("john@example.com");
            verify(userRepository).save(any(User.class));
        }
        
        @Test
        @DisplayName("Should fail signup when email already exists")
        void shouldFailWhenEmailExists() {
            // Arrange
            SignupRequest request = new SignupRequest(
                "John", "Doe", "existing@example.com", "password123", 1L
            );
            
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(request)
            );
            
            assertEquals("Email already registered", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }
        
        @Test
        @DisplayName("Should fail signup with invalid email format")
        void shouldFailWithInvalidEmail() {
            SignupRequest request = new SignupRequest(
                "John", "Doe", "invalid-email", "password123", 1L
            );
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(request)
            );
            
            assertEquals("Invalid email format", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should fail signup with short password")
        void shouldFailWithShortPassword() {
            SignupRequest request = new SignupRequest(
                "John", "Doe", "john@example.com", "short", 1L
            );
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(request)
            );
            
            assertEquals("Password must be at least 8 characters", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should fail signup with missing first name")
        void shouldFailWithMissingFirstName() {
            SignupRequest request = new SignupRequest(
                "", "Doe", "john@example.com", "password123", 1L
            );
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(request)
            );
            
            assertEquals("First name is required", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should fail signup with missing campus ID")
        void shouldFailWithMissingCampusId() {
            SignupRequest request = new SignupRequest(
                "John", "Doe", "john@example.com", "password123", null
            );
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(request)
            );
            
            assertEquals("Campus ID is required", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Login Tests")
    class LoginTests {
        
        @Test
        @DisplayName("Should fail login with non-existent email")
        void shouldFailWithNonExistentEmail() {
            // Arrange
            LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");
            
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
            );
            
            assertEquals("Invalid email or password", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should fail login with missing email")
        void shouldFailWithMissingEmail() {
            LoginRequest request = new LoginRequest("", "password123");
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
            );
            
            assertEquals("Email is required", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should fail login with missing password")
        void shouldFailWithMissingPassword() {
            LoginRequest request = new LoginRequest("john@example.com", "");
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
            );
            
            assertEquals("Password is required", exception.getMessage());
        }
    }
}
