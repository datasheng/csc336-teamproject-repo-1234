package com.campusevents.model;

/**
 * Organization model representing a student organization in the system.
 * 
 * This is a simple POJO (Plain Old Java Object) - NO JPA annotations.
 * All database operations are handled by raw SQL in OrganizationRepository.
 */
public class Organization {
    
    private Long id;
    private String name;
    private String description;
    
    // Default constructor
    public Organization() {}
    
    // Constructor for creating new organizations (no ID yet)
    public Organization(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Full constructor
    public Organization(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    @Override
    public String toString() {
        return "Organization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
