package com.campusevents.controller;

import com.campusevents.dto.ErrorResponse;
import com.campusevents.dto.UpdateUserProfileRequest;
import com.campusevents.dto.UserProfileDTO;
import com.campusevents.model.User;
import com.campusevents.security.CurrentUser;
import com.campusevents.util.SqlExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for user profile endpoints.
 * 
 * Endpoints:
 * - GET /api/users/me - Get current user profile (authenticated)
 * - PUT /api/users/me - Update current user profile (authenticated)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final SqlExecutor sqlExecutor;
    
    public UserController(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }
    
    /**
     * Get the current user's profile.
     * 
     * Requires valid JWT token in Authorization header.
     * 
     * @param user The authenticated user (injected by @CurrentUser)
     * @return User profile with campus details
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserProfile(@CurrentUser User user) {
        try {
            String sql = "SELECT u.id, u.first_name, u.last_name, u.email, u.campus_id, c.name as campus_name " +
                         "FROM \"user\" u JOIN campus c ON u.campus_id = c.id WHERE u.id = ?";
            List<Map<String, Object>> results = sqlExecutor.executeQuery(sql, new Object[]{user.getId()});
            
            if (results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "User not found", 404)
                );
            }
            
            Map<String, Object> row = results.get(0);
            UserProfileDTO profile = new UserProfileDTO(
                ((Number) row.get("id")).longValue(),
                (String) row.get("first_name"),
                (String) row.get("last_name"),
                (String) row.get("email"),
                ((Number) row.get("campus_id")).longValue(),
                (String) row.get("campus_name")
            );
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while fetching user profile", 500)
            );
        }
    }
    
    /**
     * Update the current user's profile.
     * 
     * Requires valid JWT token in Authorization header.
     * 
     * @param user The authenticated user (injected by @CurrentUser)
     * @param request The update request with firstName, lastName, and campusId
     * @return Updated user profile with campus details
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUserProfile(
            @CurrentUser User user,
            @RequestBody UpdateUserProfileRequest request) {
        try {
            // Validate request
            if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "First name is required", 400)
                );
            }
            if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "Last name is required", 400)
                );
            }
            if (request.getCampusId() == null) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "Campus ID is required", 400)
                );
            }
            
            // Update the user
            String updateSql = "UPDATE \"user\" SET first_name = ?, last_name = ?, campus_id = ? WHERE id = ?";
            int rowsAffected = sqlExecutor.executeUpdate(updateSql, new Object[]{
                request.getFirstName().trim(),
                request.getLastName().trim(),
                request.getCampusId(),
                user.getId()
            });
            
            if (rowsAffected == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "User not found", 404)
                );
            }
            
            // Fetch and return the updated profile
            String selectSql = "SELECT u.id, u.first_name, u.last_name, u.email, u.campus_id, c.name as campus_name " +
                               "FROM \"user\" u JOIN campus c ON u.campus_id = c.id WHERE u.id = ?";
            List<Map<String, Object>> results = sqlExecutor.executeQuery(selectSql, new Object[]{user.getId()});
            
            if (results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "User not found", 404)
                );
            }
            
            Map<String, Object> row = results.get(0);
            UserProfileDTO profile = new UserProfileDTO(
                ((Number) row.get("id")).longValue(),
                (String) row.get("first_name"),
                (String) row.get("last_name"),
                (String) row.get("email"),
                ((Number) row.get("campus_id")).longValue(),
                (String) row.get("campus_name")
            );
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while updating user profile", 500)
            );
        }
    }
}
