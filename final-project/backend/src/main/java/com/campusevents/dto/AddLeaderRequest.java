package com.campusevents.dto;

/**
 * Request DTO for adding a leader to an organization.
 */
public class AddLeaderRequest {
    
    private String email;
    
    // Default constructor
    public AddLeaderRequest() {}
    
    public AddLeaderRequest(String email) {
        this.email = email;
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
