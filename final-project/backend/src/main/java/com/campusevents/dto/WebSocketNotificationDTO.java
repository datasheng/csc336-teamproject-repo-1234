package com.campusevents.dto;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for outgoing WebSocket messages to frontend clients.
 * 
 * Standardized format for all messages sent to clients.
 */
public class WebSocketNotificationDTO {
    
    private String type;
    private Long eventId;
    private Long campusId;
    private Long userId;
    private String message;
    private Map<String, Object> data;
    private Instant timestamp;
    
    public WebSocketNotificationDTO() {
        this.timestamp = Instant.now();
    }
    
    public WebSocketNotificationDTO(String type) {
        this();
        this.type = type;
    }
    
    public static WebSocketNotificationDTO eventCreated(Long eventId, Long campusId, Long organizerId) {
        WebSocketNotificationDTO dto = new WebSocketNotificationDTO("EVENT_CREATED");
        dto.setEventId(eventId);
        dto.setCampusId(campusId);
        dto.setData(Map.of("organizerId", organizerId));
        return dto;
    }
    
    public static WebSocketNotificationDTO eventUpdated(Long eventId) {
        WebSocketNotificationDTO dto = new WebSocketNotificationDTO("EVENT_UPDATED");
        dto.setEventId(eventId);
        return dto;
    }
    
    public static WebSocketNotificationDTO ticketPurchased(Long eventId, Long userId, String ticketType) {
        WebSocketNotificationDTO dto = new WebSocketNotificationDTO("TICKET_PURCHASED");
        dto.setEventId(eventId);
        dto.setUserId(userId);
        dto.setData(Map.of("ticketType", ticketType, "status", "confirmed"));
        return dto;
    }
    
    public static WebSocketNotificationDTO capacityUpdated(Long eventId) {
        WebSocketNotificationDTO dto = new WebSocketNotificationDTO("CAPACITY_UPDATED");
        dto.setEventId(eventId);
        return dto;
    }
    
    public static WebSocketNotificationDTO subscriptionConfirmed(String subscriptionType, Long targetId) {
        WebSocketNotificationDTO dto = new WebSocketNotificationDTO("SUBSCRIPTION_CONFIRMED");
        dto.setMessage("Successfully subscribed to " + subscriptionType + ": " + targetId);
        dto.setData(Map.of("subscriptionType", subscriptionType, "targetId", targetId));
        return dto;
    }
    
    public static WebSocketNotificationDTO error(String errorMessage) {
        WebSocketNotificationDTO dto = new WebSocketNotificationDTO("ERROR");
        dto.setMessage(errorMessage);
        return dto;
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
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
