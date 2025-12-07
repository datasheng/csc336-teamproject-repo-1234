package com.campusevents.dto;

/**
 * DTO for campus list data.
 * Contains basic campus information.
 */
public class CampusDTO {
    
    private Long id;
    private String name;
    private String address;
    private String zipCode;
    private String city;
    
    // Default constructor
    public CampusDTO() {}
    
    public CampusDTO(Long id, String name, String address, String zipCode, String city) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.zipCode = zipCode;
        this.city = city;
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getZipCode() {
        return zipCode;
    }
    
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
}
