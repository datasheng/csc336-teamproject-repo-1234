package com.campusevents.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for creating a new event.
 */
public class CreateEventRequest {
    
    private Long organizerId;
    private Long campusId;
    private Integer capacity;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<CostDTO> costs;
    private List<String> tags = new ArrayList<>();
    
    // Default constructor
    public CreateEventRequest() {}
    
    // Getters and Setters
    public Long getOrganizerId() {
        return organizerId;
    }
    
    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }
    
    public Long getCampusId() {
        return campusId;
    }
    
    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }
    
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
    
    public List<CostDTO> getCosts() {
        return costs;
    }
    
    public void setCosts(List<CostDTO> costs) {
        this.costs = costs;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }
}
