package com.campusevents.controller;

import com.campusevents.dto.WebSocketMessageDTO;
import com.campusevents.dto.WebSocketNotificationDTO;
import com.campusevents.service.PubSubService;
import com.campusevents.service.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket controller for handling bidirectional communication with frontend clients.
 * 
 * Incoming messages from frontend are published to Pub/Sub (source of truth).
 * The PubSubSubscriberService then receives and broadcasts to all connected clients.
 * 
 * This ensures:
 * 1. All messages go through Pub/Sub for consistency
 * 2. Multiple backend instances stay synchronized
 * 3. Message ordering is preserved via Pub/Sub
 */
@Controller
public class WebSocketController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    
    private final PubSubService pubSubService;
    private final WebSocketSessionManager sessionManager;
    
    public WebSocketController(PubSubService pubSubService, WebSocketSessionManager sessionManager) {
        this.pubSubService = pubSubService;
        this.sessionManager = sessionManager;
    }
    
    /**
     * Handle subscription requests from frontend.
     * Client sends: /app/subscribe/event/{eventId}
     * 
     * @param eventId The event to subscribe to
     * @param headerAccessor WebSocket headers for session info
     * @return Confirmation message sent to user's private queue
     */
    @MessageMapping("/subscribe/event/{eventId}")
    @SendToUser("/queue/notifications")
    public WebSocketNotificationDTO subscribeToEvent(
            @DestinationVariable Long eventId,
            SimpMessageHeaderAccessor headerAccessor) {
        
        String sessionId = headerAccessor.getSessionId();
        sessionManager.subscribeToEvent(sessionId, eventId);
        
        logger.info("Session {} subscribed to event {}", sessionId, eventId);
        
        return WebSocketNotificationDTO.subscriptionConfirmed("event", eventId);
    }
    
    /**
     * Handle campus subscription requests from frontend.
     * Client sends: /app/subscribe/campus/{campusId}
     * 
     * @param campusId The campus to subscribe to
     * @param headerAccessor WebSocket headers for session info
     * @return Confirmation message sent to user's private queue
     */
    @MessageMapping("/subscribe/campus/{campusId}")
    @SendToUser("/queue/notifications")
    public WebSocketNotificationDTO subscribeToCampus(
            @DestinationVariable Long campusId,
            SimpMessageHeaderAccessor headerAccessor) {
        
        String sessionId = headerAccessor.getSessionId();
        sessionManager.subscribeToCampus(sessionId, campusId);
        
        logger.info("Session {} subscribed to campus {}", sessionId, campusId);
        
        return WebSocketNotificationDTO.subscriptionConfirmed("campus", campusId);
    }
    
    /**
     * Handle generic messages from frontend that should be published to Pub/Sub.
     * Client sends: /app/publish
     * 
     * This allows frontend to publish events through the backend to Pub/Sub,
     * which then gets distributed to all connected clients.
     * 
     * @param message The message to publish
     * @param headerAccessor WebSocket headers
     * @return Confirmation sent to user's queue
     */
    @MessageMapping("/publish")
    @SendToUser("/queue/notifications")
    public WebSocketNotificationDTO publishMessage(
            @Payload WebSocketMessageDTO message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        String sessionId = headerAccessor.getSessionId();
        Long userId = sessionManager.getUserIdForSession(sessionId);
        
        logger.info("Session {} publishing message type: {}", sessionId, message.getType());
        
        try {
            // Add user context if available
            if (userId != null) {
                message.setUserId(userId);
            }
            
            // Publish to Pub/Sub - this is the source of truth
            // PubSubSubscriberService will receive and broadcast to all clients
            String messageId = pubSubService.publishMessage(Map.of(
                "type", message.getType(),
                "eventId", message.getEventId() != null ? message.getEventId() : 0,
                "campusId", message.getCampusId() != null ? message.getCampusId() : 0,
                "userId", message.getUserId() != null ? message.getUserId() : 0,
                "payload", message.getPayload() != null ? message.getPayload() : Map.of()
            ));
            
            WebSocketNotificationDTO response = new WebSocketNotificationDTO("PUBLISH_CONFIRMED");
            response.setMessage("Message published successfully");
            response.setData(Map.of("messageId", messageId != null ? messageId : "disabled"));
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to publish message", e);
            return WebSocketNotificationDTO.error("Failed to publish message: " + e.getMessage());
        }
    }
    
    /**
     * Handle ping/heartbeat from frontend to keep connection alive.
     * Client sends: /app/ping
     * 
     * @return Pong response
     */
    @MessageMapping("/ping")
    @SendToUser("/queue/notifications")
    public WebSocketNotificationDTO ping(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        
        WebSocketNotificationDTO response = new WebSocketNotificationDTO("PONG");
        response.setData(Map.of(
            "sessionId", sessionId,
            "activeConnections", sessionManager.getActiveSessionCount()
        ));
        return response;
    }
    
    /**
     * Handle user authentication/registration with WebSocket session.
     * Client sends: /app/auth with userId
     * 
     * This associates a user ID with the WebSocket session for targeted messages.
     * 
     * @param message Message containing userId
     * @param headerAccessor WebSocket headers
     * @return Confirmation
     */
    @MessageMapping("/auth")
    @SendToUser("/queue/notifications")
    public WebSocketNotificationDTO authenticate(
            @Payload WebSocketMessageDTO message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        String sessionId = headerAccessor.getSessionId();
        Long userId = message.getUserId();
        
        if (userId == null) {
            return WebSocketNotificationDTO.error("userId is required");
        }
        
        sessionManager.registerUserSession(sessionId, userId);
        
        WebSocketNotificationDTO response = new WebSocketNotificationDTO("AUTH_CONFIRMED");
        response.setUserId(userId);
        response.setMessage("Session authenticated for user " + userId);
        return response;
    }
    
    /**
     * Get current connection status and stats.
     * Client sends: /app/status
     * 
     * @return Status information
     */
    @MessageMapping("/status")
    @SendToUser("/queue/notifications")
    public WebSocketNotificationDTO getStatus(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        Long userId = sessionManager.getUserIdForSession(sessionId);
        
        WebSocketNotificationDTO response = new WebSocketNotificationDTO("STATUS");
        response.setUserId(userId);
        response.setData(Map.of(
            "sessionId", sessionId,
            "authenticated", userId != null,
            "activeConnections", sessionManager.getActiveSessionCount(),
            "activeUsers", sessionManager.getActiveUserCount()
        ));
        return response;
    }
}
