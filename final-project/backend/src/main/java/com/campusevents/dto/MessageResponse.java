package com.campusevents.dto;

/**
 * Simple response DTO containing a message.
 */
public class MessageResponse {
    
    private String message;
    
    // Default constructor
    public MessageResponse() {}
    
    public MessageResponse(String message) {
        this.message = message;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
