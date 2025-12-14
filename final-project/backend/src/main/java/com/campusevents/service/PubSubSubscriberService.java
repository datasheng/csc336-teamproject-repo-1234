package com.campusevents.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Service that subscribes to Google Cloud Pub/Sub and forwards messages to WebSocket clients.
 * 
 * This is the bridge between Pub/Sub (source of truth) and connected frontend clients.
 * 
 * Message flow:
 * 1. Pub/Sub message received
 * 2. Parse message type (EVENT_CREATED, EVENT_UPDATED, TICKET_PURCHASED, etc.)
 * 3. Forward to appropriate WebSocket destination (/topic/events, /queue/tickets, etc.)
 */
@Service
public class PubSubSubscriberService {
    
    private static final Logger logger = LoggerFactory.getLogger(PubSubSubscriberService.class);
    
    private final String projectId;
    private final String subscriptionId;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    
    private Subscriber subscriber;
    
    public PubSubSubscriberService(
            @Value("${app.gcp.project-id:}") String projectId,
            @Value("${app.pubsub.subscription:event-updates-sub}") String subscriptionId,
            SimpMessagingTemplate messagingTemplate,
            WebSocketSessionManager sessionManager) {
        this.projectId = projectId;
        this.subscriptionId = subscriptionId;
        this.messagingTemplate = messagingTemplate;
        this.sessionManager = sessionManager;
        this.objectMapper = new ObjectMapper();
        this.enabled = projectId != null && !projectId.isBlank();
        
        if (!enabled) {
            logger.warn("Pub/Sub Subscriber is disabled - GCP_PROJECT_ID not configured");
        }
    }
    
    /**
     * Start the Pub/Sub subscriber when the application starts.
     */
    @PostConstruct
    public void startSubscriber() {
        if (!enabled) {
            logger.info("Pub/Sub subscriber not started - disabled");
            return;
        }
        
        try {
            ProjectSubscriptionName subscriptionName = 
                ProjectSubscriptionName.of(projectId, subscriptionId);
            
            MessageReceiver receiver = this::handleMessage;
            
            subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
            subscriber.startAsync().awaitRunning();
            
            logger.info("Pub/Sub subscriber started for subscription: {}", subscriptionId);
            
        } catch (Exception e) {
            logger.error("Failed to start Pub/Sub subscriber", e);
            // Don't throw - allow app to start even if subscriber fails
        }
    }
    
    /**
     * Handle incoming Pub/Sub messages and forward to WebSocket clients.
     */
    private void handleMessage(PubsubMessage message, AckReplyConsumer consumer) {
        try {
            String jsonData = message.getData().toStringUtf8();
            logger.debug("Received Pub/Sub message: {}", jsonData);
            
            Map<String, Object> messageData = objectMapper.readValue(
                jsonData, new TypeReference<Map<String, Object>>() {});
            
            String messageType = (String) messageData.get("type");
            
            if (messageType == null) {
                logger.warn("Received message without type: {}", jsonData);
                consumer.ack();
                return;
            }
            
            // Route message based on type
            switch (messageType) {
                case "EVENT_CREATED" -> handleEventCreated(messageData);
                case "EVENT_UPDATED" -> handleEventUpdated(messageData);
                case "TICKET_PURCHASED" -> handleTicketPurchased(messageData);
                default -> logger.warn("Unknown message type: {}", messageType);
            }
            
            consumer.ack();
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse Pub/Sub message", e);
            consumer.ack(); // Ack to prevent redelivery of malformed messages
        } catch (Exception e) {
            logger.error("Error processing Pub/Sub message", e);
            consumer.nack(); // Nack to retry
        }
    }
    
    /**
     * Handle EVENT_CREATED messages.
     * Broadcast to all connected clients and specifically to campus subscribers.
     */
    private void handleEventCreated(Map<String, Object> messageData) {
        Long eventId = toLong(messageData.get("eventId"));
        Long campusId = toLong(messageData.get("campusId"));
        Long organizerId = toLong(messageData.get("organizerId"));
        
        logger.info("Broadcasting EVENT_CREATED: eventId={}, campusId={}", eventId, campusId);
        
        // Broadcast to all clients on /topic/events
        messagingTemplate.convertAndSend("/topic/events", Map.of(
            "type", "EVENT_CREATED",
            "eventId", eventId,
            "campusId", campusId,
            "organizerId", organizerId
        ));
        
        // Also send to campus-specific topic
        if (campusId != null) {
            messagingTemplate.convertAndSend("/topic/campus/" + campusId, Map.of(
                "type", "EVENT_CREATED",
                "eventId", eventId,
                "organizerId", organizerId
            ));
        }
    }
    
    /**
     * Handle EVENT_UPDATED messages.
     * Broadcast to all clients and specifically to event subscribers.
     */
    private void handleEventUpdated(Map<String, Object> messageData) {
        Long eventId = toLong(messageData.get("eventId"));
        
        logger.info("Broadcasting EVENT_UPDATED: eventId={}", eventId);
        
        // Broadcast to all clients
        messagingTemplate.convertAndSend("/topic/events", Map.of(
            "type", "EVENT_UPDATED",
            "eventId", eventId
        ));
        
        // Also send to event-specific topic for clients viewing this event
        if (eventId != null) {
            messagingTemplate.convertAndSend("/topic/event/" + eventId, Map.of(
                "type", "EVENT_UPDATED",
                "eventId", eventId
            ));
        }
    }
    
    /**
     * Handle TICKET_PURCHASED messages.
     * Send to the specific user who purchased and broadcast capacity update.
     */
    private void handleTicketPurchased(Map<String, Object> messageData) {
        Long eventId = toLong(messageData.get("eventId"));
        Long userId = toLong(messageData.get("userId"));
        String ticketType = (String) messageData.get("ticketType");
        
        logger.info("Processing TICKET_PURCHASED: eventId={}, userId={}", eventId, userId);
        
        // Send confirmation to the specific user
        if (userId != null) {
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/tickets",
                Map.of(
                    "type", "TICKET_PURCHASED",
                    "eventId", eventId,
                    "ticketType", ticketType,
                    "status", "confirmed"
                )
            );
        }
        
        // Broadcast capacity update to all clients viewing this event
        if (eventId != null) {
            messagingTemplate.convertAndSend("/topic/event/" + eventId, Map.of(
                "type", "CAPACITY_UPDATED",
                "eventId", eventId
            ));
        }
    }
    
    /**
     * Utility to safely convert to Long.
     */
    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Stop the subscriber on application shutdown.
     */
    @PreDestroy
    public void stopSubscriber() {
        if (subscriber != null) {
            try {
                subscriber.stopAsync().awaitTerminated(30, TimeUnit.SECONDS);
                logger.info("Pub/Sub subscriber stopped");
            } catch (TimeoutException e) {
                logger.warn("Timeout waiting for Pub/Sub subscriber to stop");
            }
        }
    }
    
    /**
     * Check if the subscriber is running.
     */
    public boolean isRunning() {
        return subscriber != null && subscriber.isRunning();
    }
    
    /**
     * Check if the subscriber is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }
}
