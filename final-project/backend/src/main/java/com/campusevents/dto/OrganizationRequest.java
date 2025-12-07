package com.campusevents.dto;

/**
 * Request DTO for creating or updating an organization.
 */
public class OrganizationRequest {
    
    private String name;
    private String description;
    
    // Default constructor
    public OrganizationRequest() {}
    
    public OrganizationRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
