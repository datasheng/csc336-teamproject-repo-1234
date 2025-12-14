package com.campusevents.config;

import com.campusevents.service.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * Listener for WebSocket session lifecycle events.
 * 
 * Tracks when clients connect, disconnect, and subscribe to topics.
 * Updates the WebSocketSessionManager to maintain session state.
 */
@Component
public class WebSocketEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    
    private final WebSocketSessionManager sessionManager;
    
    public WebSocketEventListener(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    
    /**
     * Called when a new WebSocket client connects.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        sessionManager.registerSession(sessionId);
        
        logger.info("New WebSocket connection established. Session ID: {}", sessionId);
    }
    
    /**
     * Called when a WebSocket client disconnects.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        sessionManager.unregisterSession(sessionId);
        
        logger.info("WebSocket connection closed. Session ID: {}", sessionId);
    }
    
    /**
     * Called when a client subscribes to a topic.
     * Can be used for logging/monitoring subscription patterns.
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        logger.debug("Session {} subscribed to {}", sessionId, destination);
    }
}
