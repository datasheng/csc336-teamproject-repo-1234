package com.campusevents.model;

/**
 * User model representing a user in the system.
 * 
 * This is a simple POJO (Plain Old Java Object) - NO JPA annotations.
 * All database operations are handled by raw SQL in UserRepository.
 */
public class User {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;  // BCrypt hashed
    private Long campusId;
    
    // Default constructor
    public User() {}
    
    // Constructor for creating new users (no ID yet)
    public User(String firstName, String lastName, String email, String password, Long campusId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.campusId = campusId;
    }
    
    // Full constructor
    public User(Long id, String firstName, String lastName, String email, String password, Long campusId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.campusId = campusId;
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
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", campusId=" + campusId +
                '}';
    }
}
