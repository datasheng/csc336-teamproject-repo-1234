package com.campusevents.dto;

/**
 * Request DTO for updating user profile.
 */
public class UpdateUserProfileRequest {
    
    private String firstName;
    private String lastName;
    private Long campusId;
    
    // Default constructor
    public UpdateUserProfileRequest() {}
    
    public UpdateUserProfileRequest(String firstName, String lastName, Long campusId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.campusId = campusId;
    }
    
    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public Long getCampusId() {
        return campusId;
    }
    
    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }
}
