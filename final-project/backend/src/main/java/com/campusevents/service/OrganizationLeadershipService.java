package com.campusevents.service;

import com.campusevents.dto.LeaderDTO;
import com.campusevents.repository.OrganizationLeadershipRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for organization leadership management.
 * Handles business logic for adding, removing, and listing leaders.
 */
@Service
public class OrganizationLeadershipService {
    
    private final OrganizationLeadershipRepository leadershipRepository;
    
    public OrganizationLeadershipService(OrganizationLeadershipRepository leadershipRepository) {
        this.leadershipRepository = leadershipRepository;
    }
    
    /**
     * Get all leaders for an organization.
     * 
     * @param orgId The organization ID
     * @return List of leaders for the organization
     */
    public List<LeaderDTO> getLeaders(Long orgId) {
        return leadershipRepository.findLeadersByOrgId(orgId);
    }
    
    /**
     * Check if an organization exists.
     * 
     * @param orgId The organization ID
     * @return true if the organization exists
     */
    public boolean organizationExists(Long orgId) {
        return leadershipRepository.organizationExists(orgId);
    }
    
    /**
     * Check if a user is a leader of an organization.
     * 
     * @param userId The user ID
     * @param orgId The organization ID
     * @return true if the user is a leader
     */
    public boolean isLeader(Long userId, Long orgId) {
        return leadershipRepository.isLeader(userId, orgId);
    }
    
    /**
     * Add a user as a leader of an organization by email.
     * 
     * @param email The email of the user to add
     * @param orgId The organization ID
     * @return Result of the operation
     */
    public AddLeaderResult addLeader(String email, Long orgId) {
        // Find user by email
        Optional<Long> userIdOpt = leadershipRepository.findUserIdByEmail(email);
        
        if (userIdOpt.isEmpty()) {
            return AddLeaderResult.userNotFound();
        }
        
        Long userId = userIdOpt.get();
        
        // Check if already a leader
        if (leadershipRepository.isLeader(userId, orgId)) {
            return AddLeaderResult.alreadyLeader();
        }
        
        // Add as leader
        leadershipRepository.addLeader(userId, orgId);
        
        return AddLeaderResult.success();
    }
    
    /**
     * Remove a user as a leader of an organization.
     * 
     * @param userId The user ID to remove
     * @param orgId The organization ID
     * @return Result of the operation
     */
    public RemoveLeaderResult removeLeader(Long userId, Long orgId) {
        // Check if user is actually a leader
        if (!leadershipRepository.isLeader(userId, orgId)) {
            return RemoveLeaderResult.notALeader();
        }
        
        // Check if this is the last leader
        int leaderCount = leadershipRepository.countLeaders(orgId);
        if (leaderCount <= 1) {
            return RemoveLeaderResult.lastLeader();
        }
        
        // Remove the leader
        leadershipRepository.removeLeader(userId, orgId);
        
        return RemoveLeaderResult.success();
    }
    
    /**
     * Result class for add leader operation.
     */
    public static class AddLeaderResult {
        private final boolean success;
        private final String errorType;
        private final String message;
        
        private AddLeaderResult(boolean success, String errorType, String message) {
            this.success = success;
            this.errorType = errorType;
            this.message = message;
        }
        
        public static AddLeaderResult success() {
            return new AddLeaderResult(true, null, "Leader added successfully");
        }
        
        public static AddLeaderResult userNotFound() {
            return new AddLeaderResult(false, "USER_NOT_FOUND", "User with this email not found");
        }
        
        public static AddLeaderResult alreadyLeader() {
            return new AddLeaderResult(false, "ALREADY_LEADER", "User is already a leader of this organization");
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getErrorType() {
            return errorType;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * Result class for remove leader operation.
     */
    public static class RemoveLeaderResult {
        private final boolean success;
        private final String errorType;
        private final String message;
        
        private RemoveLeaderResult(boolean success, String errorType, String message) {
            this.success = success;
            this.errorType = errorType;
            this.message = message;
        }
        
        public static RemoveLeaderResult success() {
            return new RemoveLeaderResult(true, null, "Leader removed successfully");
        }
        
        public static RemoveLeaderResult notALeader() {
            return new RemoveLeaderResult(false, "NOT_A_LEADER", "User is not a leader of this organization");
        }
        
        public static RemoveLeaderResult lastLeader() {
            return new RemoveLeaderResult(false, "LAST_LEADER", "Cannot remove the last leader of an organization");
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getErrorType() {
            return errorType;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
