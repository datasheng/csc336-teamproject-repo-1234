package com.campusevents.dto;

/**
 * DTO for user profile data.
 * Contains user information with campus details.
 */
public class UserProfileDTO {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Long campusId;
    private String campusName;
    
    // Default constructor
    public UserProfileDTO() {}
    
    public UserProfileDTO(Long id, String firstName, String lastName, String email, Long campusId, String campusName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.campusId = campusId;
        this.campusName = campusName;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Long getCampusId() {
        return campusId;
    }
    
    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }
    
    public String getCampusName() {
        return campusName;
    }
    
    public void setCampusName(String campusName) {
        this.campusName = campusName;
    }
}
