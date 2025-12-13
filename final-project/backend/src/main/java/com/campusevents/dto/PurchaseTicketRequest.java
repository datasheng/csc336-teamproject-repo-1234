package com.campusevents.dto;

/**
 * DTO for purchasing a ticket.
 */
public class PurchaseTicketRequest {
    
    private Long eventId;
    private String type;
    
    // Default constructor
    public PurchaseTicketRequest() {}
    
    public PurchaseTicketRequest(Long eventId, String type) {
        this.eventId = eventId;
        this.type = type;
    }
    
    // Getters and Setters
    public Long getEventId() {
        return eventId;
    }
    
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}
