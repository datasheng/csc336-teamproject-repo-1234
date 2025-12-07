package com.campusevents.controller;

import com.campusevents.dto.AuthResponse;
import com.campusevents.dto.ErrorResponse;
import com.campusevents.dto.LoginRequest;
import com.campusevents.dto.SignupRequest;
import com.campusevents.model.User;
import com.campusevents.security.CurrentUser;
import com.campusevents.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * 
 * Endpoints:
 * - POST /api/auth/signup - Register a new user
 * - POST /api/auth/login - Authenticate and get JWT token
 * - GET /api/auth/me - Get current authenticated user info
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Register a new user.
     * 
     * Request body:
     * {
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "email": "john@example.com",
     *   "password": "securepassword",
     *   "campusId": 1
     * }
     * 
     * @param request The signup request
     * @return JWT token and user info
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            AuthResponse response = authService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Bad Request", e.getMessage(), 400)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred during signup", 500)
            );
        }
    }
    
    /**
     * Authenticate a user and return a JWT token.
     * 
     * Request body:
     * {
     *   "email": "john@example.com",
     *   "password": "securepassword"
     * }
     * 
     * @param request The login request
     * @return JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse("Unauthorized", e.getMessage(), 401)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred during login", 500)
            );
        }
    }
    
    /**
     * Get the currently authenticated user's information.
     * 
     * Requires valid JWT token in Authorization header.
     * 
     * @param user The authenticated user (injected by @CurrentUser)
     * @return User information (without password)
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@CurrentUser User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse("Unauthorized", "Authentication required", 401)
            );
        }
        
        // Return user info without password
        return ResponseEntity.ok(new UserInfo(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getCampusId()
        ));
    }
    
    /**
     * Simple DTO for user info response (excludes password).
     */
    public record UserInfo(Long id, String firstName, String lastName, String email, Long campusId) {}
}
