package com.campusevents.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for updating an existing event.
 */
public class UpdateEventRequest {
    
    private Integer capacity;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> tags;
    
    // Default constructor
    public UpdateEventRequest() {}
    
    // Getters and Setters
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
