package com.campusevents.security;

import com.campusevents.model.User;
import com.campusevents.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * JWT Authentication Filter.
 * 
 * This filter intercepts all HTTP requests and validates JWT tokens
 * in the Authorization header. If a valid token is found, it stores
 * the authenticated user in the request attributes for use by controllers.
 * 
 * Token format: "Bearer <token>"
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                Optional<User> userOpt = userRepository.findById(userId);
                
                if (userOpt.isPresent()) {
                    // Store authenticated user in request for @CurrentUser annotation
                    request.setAttribute("currentUser", userOpt.get());
                    request.setAttribute("userId", userId);
                    request.setAttribute("userEmail", jwtUtil.getEmailFromToken(token));
                    request.setAttribute("userCampusId", jwtUtil.getCampusIdFromToken(token));
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Don't filter public endpoints
        return path.startsWith("/api/auth/") || 
               path.equals("/api/health") ||
               path.startsWith("/api/public/");
    }
}
