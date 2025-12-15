package com.campusevents.service;

import com.campusevents.dto.AuthResponse;
import com.campusevents.dto.LoginRequest;
import com.campusevents.dto.OrganizationRequest;
import com.campusevents.dto.SignupRequest;
import com.campusevents.model.User;
import com.campusevents.repository.UserRepository;
import com.campusevents.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service for user authentication operations.
 * 
 * Handles:
 * - User registration with password hashing
 * - User login with password verification
 * - JWT token generation
 */
@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OrganizationService organizationService;
    
    // Email validation regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    // Minimum password length
    private static final int MIN_PASSWORD_LENGTH = 8;
    
    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, OrganizationService organizationService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.organizationService = organizationService;
    }
    
    /**
     * Register a new user. Optionally creates an organization.
     * 
     * @param request The signup request with user details
     * @return AuthResponse with JWT token
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // Validate input
        validateSignupRequest(request);
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Hash password with BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        
        // Create and save user
        User user = new User(
            request.getFirstName(),
            request.getLastName(),
            request.getEmail().toLowerCase(),
            hashedPassword,
            request.getCampusId()
        );
        
        user = userRepository.save(user);
        
        // Create organization if requested
        if (Boolean.TRUE.equals(request.getCreateOrganization()) && 
            request.getOrganizationName() != null && 
            !request.getOrganizationName().isBlank()) {
            OrganizationRequest orgRequest = new OrganizationRequest(
                request.getOrganizationName(),
                request.getOrganizationDescription()
            );
            organizationService.createOrganization(orgRequest, user.getId());
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getCampusId());
        
        return new AuthResponse(
            token,
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getCampusId(),
            user.getIsAdmin() != null ? user.getIsAdmin() : false
        );
    }
    
    /**
     * Authenticate a user and return a JWT token.
     * 
     * @param request The login request with credentials
     * @return AuthResponse with JWT token
     * @throws IllegalArgumentException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        // Validate input
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail().toLowerCase());
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        User user = userOpt.get();
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getCampusId());
        
        return new AuthResponse(
            token,
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getCampusId(),
            user.getIsAdmin() != null ? user.getIsAdmin() : false
        );
    }
    
    /**
     * Validate signup request fields.
     */
    private void validateSignupRequest(SignupRequest request) {
        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (request.getCampusId() == null) {
            throw new IllegalArgumentException("Campus ID is required");
        }
    }
}
