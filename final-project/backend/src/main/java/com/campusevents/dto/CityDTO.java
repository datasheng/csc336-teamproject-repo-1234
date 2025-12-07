package com.campusevents.dto;

/**
 * DTO for city data.
 * Contains city name and country.
 */
public class CityDTO {
    
    private String city;
    private String country;
    
    // Default constructor
    public CityDTO() {}
    
    public CityDTO(String city, String country) {
        this.city = city;
        this.country = country;
    }
    
    // Getters and Setters
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
