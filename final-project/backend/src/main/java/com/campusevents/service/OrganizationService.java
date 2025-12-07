package com.campusevents.service;

import com.campusevents.dto.OrganizationDTO;
import com.campusevents.dto.OrganizationRequest;
import com.campusevents.model.Organization;
import com.campusevents.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for organization-related operations.
 * Handles business logic for creating, reading, and updating organizations.
 */
@Service
public class OrganizationService {
    
    private final OrganizationRepository organizationRepository;
    
    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }
    
    /**
     * Create a new organization.
     * The creator is automatically added as a leader.
     * 
     * @param request The organization creation request
     * @param creatorUserId The ID of the user creating the organization
     * @return The created organization DTO
     */
    public OrganizationDTO createOrganization(OrganizationRequest request, Long creatorUserId) {
        // Create and save the organization
        Organization organization = new Organization(request.getName(), request.getDescription());
        organization = organizationRepository.save(organization);
        
        // Add the creator as a leader
        organizationRepository.addLeader(creatorUserId, organization.getId());
        
        return toDTO(organization);
    }
    
    /**
     * Get an organization by ID.
     * 
     * @param id The organization ID
     * @return Optional containing the organization DTO if found
     */
    public Optional<OrganizationDTO> getOrganization(Long id) {
        return organizationRepository.findById(id).map(this::toDTO);
    }
    
    /**
     * Update an existing organization.
     * 
     * @param id The organization ID
     * @param request The update request
     * @return Optional containing the updated organization DTO if found
     */
    public Optional<OrganizationDTO> updateOrganization(Long id, OrganizationRequest request) {
        Optional<Organization> existingOrg = organizationRepository.findById(id);
        
        if (existingOrg.isEmpty()) {
            return Optional.empty();
        }
        
        Organization organization = existingOrg.get();
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        
        organizationRepository.update(organization);
        
        return Optional.of(toDTO(organization));
    }
    
    /**
     * Check if a user is a leader of an organization.
     * 
     * @param userId The user ID
     * @param orgId The organization ID
     * @return true if the user is a leader of the organization
     */
    public boolean isLeader(Long userId, Long orgId) {
        return organizationRepository.isLeader(userId, orgId);
    }
    
    /**
     * Convert Organization model to DTO.
     */
    private OrganizationDTO toDTO(Organization organization) {
        return new OrganizationDTO(
            organization.getId(),
            organization.getName(),
            organization.getDescription()
        );
    }
}
