package com.campusevents.controller;

import com.campusevents.dto.ErrorResponse;
import com.campusevents.dto.OrganizationDTO;
import com.campusevents.dto.OrganizationRequest;
import com.campusevents.model.User;
import com.campusevents.security.CurrentUser;
import com.campusevents.service.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for organization endpoints.
 * 
 * Endpoints:
 * - POST /api/organizations - Create a new organization (authenticated)
 * - GET /api/organizations/{id} - Get organization by ID
 * - PUT /api/organizations/{id} - Update organization (authenticated, must be org leader)
 */
@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {
    
    private final OrganizationService organizationService;
    
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }
    
    /**
     * Create a new organization.
     * 
     * The authenticated user becomes a leader of the organization.
     * 
     * @param user The authenticated user (injected by @CurrentUser)
     * @param request The organization creation request (name, description)
     * @return Created organization with id
     */
    @PostMapping
    public ResponseEntity<?> createOrganization(
            @CurrentUser User user,
            @RequestBody OrganizationRequest request) {
        try {
            // Validate request
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "Name is required", 400)
                );
            }
            
            OrganizationDTO organization = organizationService.createOrganization(request, user.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(organization);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while creating organization", 500)
            );
        }
    }
    
    /**
     * Get an organization by ID.
     * 
     * @param id The organization ID
     * @return Organization details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrganization(@PathVariable Long id) {
        try {
            Optional<OrganizationDTO> organization = organizationService.getOrganization(id);
            
            if (organization.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "Organization not found", 404)
                );
            }
            
            return ResponseEntity.ok(organization.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while fetching organization", 500)
            );
        }
    }
    
    /**
     * Update an organization.
     * 
     * Only organization leaders can update the organization.
     * 
     * @param user The authenticated user (injected by @CurrentUser)
     * @param id The organization ID
     * @param request The update request (name, description)
     * @return Updated organization
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrganization(
            @CurrentUser User user,
            @PathVariable Long id,
            @RequestBody OrganizationRequest request) {
        try {
            // Validate request
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "Name is required", 400)
                );
            }
            
            // Check if user is a leader of the organization
            if (!organizationService.isLeader(user.getId(), id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse("Forbidden", "You must be a leader of this organization to update it", 403)
                );
            }
            
            Optional<OrganizationDTO> updated = organizationService.updateOrganization(id, request);
            
            if (updated.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "Organization not found", 404)
                );
            }
            
            return ResponseEntity.ok(updated.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while updating organization", 500)
            );
        }
    }
}
