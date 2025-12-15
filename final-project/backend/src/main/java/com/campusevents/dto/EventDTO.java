package com.campusevents.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for event response with full details.
 */
public class EventDTO {
    
    private Long id;
    private Long organizerId;
    private String organizerName;
    private Long campusId;
    private String campusName;
    private Integer capacity;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<CostDTO> costs;
    private Long ticketsSold;
    private Integer availableCapacity;
    private List<String> tags = new ArrayList<>();
    
    // Default constructor
    public EventDTO() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getOrganizerId() {
        return organizerId;
    }
    
    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }
    
    public String getOrganizerName() {
        return organizerName;
    }
    
    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
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
    
    public Long getTicketsSold() {
        return ticketsSold;
    }
    
    public void setTicketsSold(Long ticketsSold) {
        this.ticketsSold = ticketsSold;
    }
    
    public Integer getAvailableCapacity() {
        return availableCapacity;
    }
    
    public void setAvailableCapacity(Integer availableCapacity) {
        this.availableCapacity = availableCapacity;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }
}
