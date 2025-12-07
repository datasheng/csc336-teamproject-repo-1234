package com.campusevents.dto;

/**
 * DTO for detailed campus data.
 * Contains campus information including country from joined city table.
 */
public class CampusDetailDTO {
    
    private Long id;
    private String name;
    private String address;
    private String zipCode;
    private String city;
    private String country;
    
    // Default constructor
    public CampusDetailDTO() {}
    
    public CampusDetailDTO(Long id, String name, String address, String zipCode, String city, String country) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.zipCode = zipCode;
        this.city = city;
        this.country = country;
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
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
}
