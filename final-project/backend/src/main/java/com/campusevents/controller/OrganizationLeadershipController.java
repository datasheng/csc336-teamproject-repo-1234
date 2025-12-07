package com.campusevents.controller;

import com.campusevents.dto.AddLeaderRequest;
import com.campusevents.dto.ErrorResponse;
import com.campusevents.dto.LeaderDTO;
import com.campusevents.dto.MessageResponse;
import com.campusevents.model.User;
import com.campusevents.security.CurrentUser;
import com.campusevents.service.OrganizationLeadershipService;
import com.campusevents.service.OrganizationLeadershipService.AddLeaderResult;
import com.campusevents.service.OrganizationLeadershipService.RemoveLeaderResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for organization leadership management endpoints.
 * 
 * Endpoints:
 * - GET /api/organizations/{id}/leaders - Get all leaders of an organization
 * - POST /api/organizations/{id}/leaders - Add a leader to an organization (authenticated, must be org leader)
 * - DELETE /api/organizations/{id}/leaders/{userId} - Remove a leader from an organization (authenticated, must be org leader)
 */
@RestController
@RequestMapping("/api/organizations")
public class OrganizationLeadershipController {
    
    private final OrganizationLeadershipService leadershipService;
    
    public OrganizationLeadershipController(OrganizationLeadershipService leadershipService) {
        this.leadershipService = leadershipService;
    }
    
    /**
     * Get all leaders of an organization.
     * 
     * @param id The organization ID
     * @return List of leaders
     */
    @GetMapping("/{id}/leaders")
    public ResponseEntity<?> getLeaders(@PathVariable Long id) {
        try {
            // Check if organization exists
            if (!leadershipService.organizationExists(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "Organization not found", 404)
                );
            }
            
            List<LeaderDTO> leaders = leadershipService.getLeaders(id);
            return ResponseEntity.ok(leaders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while fetching leaders", 500)
            );
        }
    }
    
    /**
     * Add a leader to an organization.
     * Only existing organization leaders can add new leaders.
     * 
     * @param user The authenticated user (injected by @CurrentUser)
     * @param id The organization ID
     * @param request The request containing the email of the user to add
     * @return Success message or error
     */
    @PostMapping("/{id}/leaders")
    public ResponseEntity<?> addLeader(
            @CurrentUser User user,
            @PathVariable Long id,
            @RequestBody AddLeaderRequest request) {
        try {
            // Validate request
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "Email is required", 400)
                );
            }
            
            // Check if organization exists
            if (!leadershipService.organizationExists(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "Organization not found", 404)
                );
            }
            
            // Check if requester is a leader
            if (!leadershipService.isLeader(user.getId(), id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse("Forbidden", "You must be a leader of this organization to add leaders", 403)
                );
            }
            
            // Add the leader
            AddLeaderResult result = leadershipService.addLeader(request.getEmail().trim(), id);
            
            if (!result.isSuccess()) {
                HttpStatus status = "USER_NOT_FOUND".equals(result.getErrorType()) 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.BAD_REQUEST;
                return ResponseEntity.status(status).body(
                    new ErrorResponse(status.getReasonPhrase(), result.getMessage(), status.value())
                );
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                new MessageResponse(result.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while adding leader", 500)
            );
        }
    }
    
    /**
     * Remove a leader from an organization.
     * Only existing organization leaders can remove leaders.
     * Cannot remove the last leader.
     * 
     * @param user The authenticated user (injected by @CurrentUser)
     * @param id The organization ID
     * @param userId The user ID to remove as leader
     * @return Success message or error
     */
    @DeleteMapping("/{id}/leaders/{userId}")
    public ResponseEntity<?> removeLeader(
            @CurrentUser User user,
            @PathVariable Long id,
            @PathVariable Long userId) {
        try {
            // Check if organization exists
            if (!leadershipService.organizationExists(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "Organization not found", 404)
                );
            }
            
            // Check if requester is a leader
            if (!leadershipService.isLeader(user.getId(), id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse("Forbidden", "You must be a leader of this organization to remove leaders", 403)
                );
            }
            
            // Remove the leader
            RemoveLeaderResult result = leadershipService.removeLeader(userId, id);
            
            if (!result.isSuccess()) {
                HttpStatus status = "LAST_LEADER".equals(result.getErrorType()) 
                    ? HttpStatus.BAD_REQUEST 
                    : HttpStatus.NOT_FOUND;
                return ResponseEntity.status(status).body(
                    new ErrorResponse(status.getReasonPhrase(), result.getMessage(), status.value())
                );
            }
            
            return ResponseEntity.ok(new MessageResponse(result.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while removing leader", 500)
            );
        }
    }
}
