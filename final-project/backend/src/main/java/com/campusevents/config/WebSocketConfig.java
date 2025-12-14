package com.campusevents.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time communication between frontend and backend.
 * 
 * Uses STOMP protocol over WebSocket for structured messaging.
 * Pub/Sub is the source of truth - WebSocket forwards messages to connected frontends.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker for broadcasting to subscribers
        // /topic - for broadcast messages (e.g., event updates visible to all)
        // /queue - for user-specific messages (e.g., ticket confirmations)
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages FROM clients TO server (via @MessageMapping)
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint that frontend connects to
        // SockJS fallback for browsers that don't support WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Raw WebSocket endpoint (no SockJS) for native WebSocket clients
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
