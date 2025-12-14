package com.campusevents.dto;

/**
 * DTO for incoming WebSocket messages from frontend clients.
 * 
 * Used for bidirectional communication where frontend can send
 * commands/messages to be published to Pub/Sub.
 */
public class WebSocketMessageDTO {
    
    private String type;
    private Long eventId;
    private Long campusId;
    private Long userId;
    private String ticketType;
    private Object payload;
    
    public WebSocketMessageDTO() {}
    
    public WebSocketMessageDTO(String type) {
        this.type = type;
    }
    
    // Getters and Setters
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Long getEventId() {
        return eventId;
    }
    
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    
    public Long getCampusId() {
        return campusId;
    }
    
    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getTicketType() {
        return ticketType;
    }
    
    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }
    
    public Object getPayload() {
        return payload;
    }
    
    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
