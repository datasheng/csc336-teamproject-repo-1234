package com.campusevents.dto;

/**
 * Request DTO for user signup.
 */
public class SignupRequest {
    
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Long campusId;
    
    // Default constructor
    public SignupRequest() {}
    
    public SignupRequest(String firstName, String lastName, String email, String password, Long campusId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Long getCampusId() {
        return campusId;
    }
    
    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }
}
