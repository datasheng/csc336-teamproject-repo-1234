package com.campusevents.dto;

import java.math.BigDecimal;

/**
 * DTO for ticket purchase confirmation.
 */
public class TicketConfirmationDTO {
    
    private Long eventId;
    private Long userId;
    private String type;
    private BigDecimal cost;
    private String eventDescription;
    private String message;
    
    // Default constructor
    public TicketConfirmationDTO() {}
    
    // Getters and Setters
    public Long getEventId() {
        return eventId;
    }
    
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public BigDecimal getCost() {
        return cost;
    }
    
    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
    
    public String getEventDescription() {
        return eventDescription;
    }
    
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
