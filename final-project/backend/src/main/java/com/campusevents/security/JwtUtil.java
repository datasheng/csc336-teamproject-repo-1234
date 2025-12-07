package com.campusevents.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for JWT token generation and validation.
 * 
 * Tokens contain:
 * - userId: The user's database ID
 * - email: The user's email address
 * - campusId: The user's campus ID
 */
@Component
public class JwtUtil {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;
    
    /**
     * Generate a JWT token for the given user.
     * 
     * @param userId The user's database ID
     * @param email The user's email
     * @param campusId The user's campus ID
     * @return The generated JWT token string
     */
    public String generateToken(Long userId, String email, Long campusId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("campusId", campusId);
        
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Validate a JWT token.
     * 
     * @param token The JWT token to validate
     * @return true if the token is valid and not expired
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Extract the user ID from a JWT token.
     * 
     * @param token The JWT token
     * @return The user ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return ((Number) claims.get("userId")).longValue();
    }
    
    /**
     * Extract the email from a JWT token.
     * 
     * @param token The JWT token
     * @return The email address
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }
    
    /**
     * Extract the campus ID from a JWT token.
     * 
     * @param token The JWT token
     * @return The campus ID
     */
    public Long getCampusIdFromToken(String token) {
        Claims claims = getClaims(token);
        return ((Number) claims.get("campusId")).longValue();
    }
    
    /**
     * Get all claims from a JWT token.
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Get the signing key from the configured secret.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
