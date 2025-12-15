package com.campusevents.service;

import com.campusevents.dto.EventDTO;
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
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    
    private Subscriber subscriber;
    private EventService eventService; // Lazy loaded to avoid circular dependency
    
    public PubSubSubscriberService(
            @Value("${app.gcp.project-id:}") String projectId,
            @Value("${app.pubsub.subscription:event-updates-sub}") String subscriptionId,
            SimpMessagingTemplate messagingTemplate,
            WebSocketSessionManager sessionManager,
            ApplicationContext applicationContext) {
        this.projectId = projectId;
        this.subscriptionId = subscriptionId;
        this.messagingTemplate = messagingTemplate;
        this.sessionManager = sessionManager;
        this.applicationContext = applicationContext;
        this.objectMapper = new ObjectMapper();
        this.enabled = projectId != null && !projectId.isBlank();
        
        if (!enabled) {
            logger.warn("Pub/Sub Subscriber is disabled - GCP_PROJECT_ID not configured");
        }
    }
    
    /**
     * Lazy getter for EventService to avoid circular dependency.
     */
    private EventService getEventService() {
        if (eventService == null) {
            eventService = applicationContext.getBean(EventService.class);
        }
        return eventService;
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
                case "EVENT_DELETED" -> handleEventDeleted(messageData);
                case "EVENT_CANCELLED" -> handleEventCancelled(messageData);
                case "TICKET_PURCHASED" -> handleTicketPurchased(messageData);
                case "ORGANIZATION_UPDATED" -> handleOrganizationUpdated(messageData);
                case "ANALYTICS_UPDATED" -> handleAnalyticsUpdated(messageData);
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
     * Broadcast full event data to all connected clients for client-side filtering.
     */
    private void handleEventCreated(Map<String, Object> messageData) {
        Long eventId = toLong(messageData.get("eventId"));
        Long campusId = toLong(messageData.get("campusId"));
        Long organizerId = toLong(messageData.get("organizerId"));
        
        logger.info("Broadcasting EVENT_CREATED: eventId={}, campusId={}", eventId, campusId);
        
        // Fetch full event data for client-side filter matching
        Map<String, Object> eventPayload = new java.util.HashMap<>();
        eventPayload.put("type", "EVENT_CREATED");
        eventPayload.put("eventId", eventId);
        eventPayload.put("campusId", campusId);
        eventPayload.put("organizerId", organizerId);
        
        try {
            Optional<EventDTO> eventOpt = getEventService().getEventById(eventId);
            if (eventOpt.isPresent()) {
                eventPayload.put("event", eventToMap(eventOpt.get()));
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch event data for EVENT_CREATED broadcast: {}", e.getMessage());
        }
        
        // Broadcast to all clients on /topic/events (dashboard)
        messagingTemplate.convertAndSend("/topic/events", eventPayload);
        
        // Also send to campus-specific topic
        if (campusId != null) {
            messagingTemplate.convertAndSend("/topic/campus/" + campusId, eventPayload);
        }
        
        // Send to organization-specific topic (for org page viewers)
        if (organizerId != null) {
            messagingTemplate.convertAndSend("/topic/organization/" + organizerId, eventPayload);
        }
    }
    
    /**
     * Handle EVENT_UPDATED messages.
     * Broadcast full event data to all clients for live updates.
     */
    private void handleEventUpdated(Map<String, Object> messageData) {
        Long eventId = toLong(messageData.get("eventId"));
        Long organizerId = toLong(messageData.get("organizerId"));
        
        logger.info("Broadcasting EVENT_UPDATED: eventId={}", eventId);
        
        // Fetch full event data for the update
        Map<String, Object> eventPayload = new java.util.HashMap<>();
        eventPayload.put("type", "EVENT_UPDATED");
        eventPayload.put("eventId", eventId);
        eventPayload.put("organizerId", organizerId);
        
        Long campusId = null;
        try {
            Optional<EventDTO> eventOpt = getEventService().getEventById(eventId);
            if (eventOpt.isPresent()) {
                EventDTO event = eventOpt.get();
                campusId = event.getCampusId();
                if (organizerId == null) {
                    organizerId = event.getOrganizerId();
                }
                eventPayload.put("event", eventToMap(event));
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch event data for EVENT_UPDATED broadcast: {}", e.getMessage());
        }
        
        // Broadcast to all clients on the dashboard
        messagingTemplate.convertAndSend("/topic/events", eventPayload);
        
        // Also send to event-specific topic for clients viewing this event
        if (eventId != null) {
            messagingTemplate.convertAndSend("/topic/event/" + eventId, eventPayload);
        }
        
        // Send to campus-specific topic
        if (campusId != null) {
            messagingTemplate.convertAndSend("/topic/campus/" + campusId, eventPayload);
        }
        
        // Send to organization-specific topic (for org page viewers)
        if (organizerId != null) {
            messagingTemplate.convertAndSend("/topic/organization/" + organizerId, eventPayload);
        }
    }
    
    /**
     * Handle TICKET_PURCHASED messages.
     * Send to the specific user who purchased and broadcast capacity update to:
     * 1. Event-specific topic (for users viewing the event page)
     * 2. Dashboard topic (for users on the events dashboard)
     * 3. Campus-specific topic (for users filtering by campus)
     */
    private void handleTicketPurchased(Map<String, Object> messageData) {
        Long eventId = toLong(messageData.get("eventId"));
        Long userId = toLong(messageData.get("userId"));
        String ticketType = (String) messageData.get("ticketType");
        Long ticketsSold = toLong(messageData.get("ticketsSold"));
        Integer remainingCapacity = toInt(messageData.get("remainingCapacity"));
        Long campusId = toLong(messageData.get("campusId"));
        
        logger.info("Processing TICKET_PURCHASED: eventId={}, userId={}, ticketsSold={}, remainingCapacity={}", 
                    eventId, userId, ticketsSold, remainingCapacity);
        
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
        
        // Build capacity update payload with actual numbers
        Map<String, Object> capacityUpdate = new java.util.HashMap<>();
        capacityUpdate.put("type", "CAPACITY_UPDATED");
        capacityUpdate.put("eventId", eventId);
        if (ticketsSold != null) {
            capacityUpdate.put("ticketsSold", ticketsSold);
        }
        if (remainingCapacity != null) {
            capacityUpdate.put("remainingCapacity", remainingCapacity);
            capacityUpdate.put("availableCapacity", remainingCapacity);
        }
        
        // Broadcast capacity update to all clients viewing this event
        if (eventId != null) {
            messagingTemplate.convertAndSend("/topic/event/" + eventId, capacityUpdate);
        }
        
        // Broadcast to all clients on the events dashboard
        messagingTemplate.convertAndSend("/topic/events", capacityUpdate);
        
        // Also send to campus-specific topic for dashboard filtering
        if (campusId != null) {
            messagingTemplate.convertAndSend("/topic/campus/" + campusId, capacityUpdate);
        }
    }
    
    /**
     * Handle EVENT_DELETED messages.
     * Notify clients to remove the event from their views.
     */
    private void handleEventDeleted(Map<String, Object> messageData) {
        Long eventId = toLong(messageData.get("eventId"));
        Long campusId = toLong(messageData.get("campusId"));
        Long organizerId = toLong(messageData.get("organizerId"));
        
        logger.info("Broadcasting EVENT_DELETED: eventId={}", eventId);
        
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("type", "EVENT_DELETED");
        payload.put("eventId", eventId);
        if (organizerId != null) {
            payload.put("organizerId", organizerId);
        }
        
        // Broadcast to dashboard
        messagingTemplate.convertAndSend("/topic/events", payload);
        
        // Send to event-specific topic
        if (eventId != null) {
            messagingTemplate.convertAndSend("/topic/event/" + eventId, payload);
        }
        
        // Send to campus-specific topic
        if (campusId != null) {
            messagingTemplate.convertAndSend("/topic/campus/" + campusId, payload);
        }
        
        // Send to organization-specific topic (for org page viewers)
        if (organizerId != null) {
            messagingTemplate.convertAndSend("/topic/organization/" + organizerId, payload);
        }
    }
    
    /**
     * Handle EVENT_CANCELLED messages.
     * Notify clients that an event has been cancelled but still exists.
     */
    private void handleEventCancelled(Map<String, Object> messageData) {
        Long eventId = toLong(messageData.get("eventId"));
        Long campusId = toLong(messageData.get("campusId"));
        Long organizerId = toLong(messageData.get("organizerId"));
        
        logger.info("Broadcasting EVENT_CANCELLED: eventId={}", eventId);
        
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("type", "EVENT_CANCELLED");
        payload.put("eventId", eventId);
        if (organizerId != null) {
            payload.put("organizerId", organizerId);
        }
        
        // Broadcast to dashboard
        messagingTemplate.convertAndSend("/topic/events", payload);
        
        // Send to event-specific topic (users viewing this event should see cancellation)
        if (eventId != null) {
            messagingTemplate.convertAndSend("/topic/event/" + eventId, payload);
        }
        
        // Send to campus-specific topic
        if (campusId != null) {
            messagingTemplate.convertAndSend("/topic/campus/" + campusId, payload);
        }
        
        // Send to organization-specific topic (for org page viewers)
        if (organizerId != null) {
            messagingTemplate.convertAndSend("/topic/organization/" + organizerId, payload);
        }
    }
    
    /**
     * Handle ANALYTICS_UPDATED messages.
     * Notify org dashboard clients to refresh their analytics data.
     */
    private void handleAnalyticsUpdated(Map<String, Object> messageData) {
        Long eventId = toLong(messageData.get("eventId"));
        Long organizerId = toLong(messageData.get("organizerId"));
        
        logger.info("Broadcasting ANALYTICS_UPDATED: eventId={}, organizerId={}", eventId, organizerId);
        
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("type", "ANALYTICS_UPDATED");
        payload.put("eventId", eventId);
        if (organizerId != null) {
            payload.put("organizerId", organizerId);
        }
        
        // Send to organization-specific analytics topic
        if (organizerId != null) {
            messagingTemplate.convertAndSend("/topic/organization/" + organizerId + "/analytics", payload);
            // Also send to general organization topic
            messagingTemplate.convertAndSend("/topic/organization/" + organizerId, payload);
        }
        
        // Send to event-specific topic as well
        if (eventId != null) {
            messagingTemplate.convertAndSend("/topic/event/" + eventId + "/analytics", payload);
        }
    }
    
    /**
     * Handle ORGANIZATION_UPDATED messages.
     * Notify clients that organization details have changed.
     */
    private void handleOrganizationUpdated(Map<String, Object> messageData) {
        Long organizationId = toLong(messageData.get("organizationId"));
        
        logger.info("Broadcasting ORGANIZATION_UPDATED: organizationId={}", organizationId);
        
        Map<String, Object> payload = Map.of(
            "type", "ORGANIZATION_UPDATED",
            "organizationId", organizationId
        );
        
        // Broadcast to all clients (org name might appear in event listings)
        messagingTemplate.convertAndSend("/topic/events", payload);
        
        // Send to organization-specific topic if clients subscribe to it
        if (organizationId != null) {
            messagingTemplate.convertAndSend("/topic/organization/" + organizationId, payload);
        }
    }
    
    /**
     * Convert an EventDTO to a Map suitable for JSON serialization over WebSocket.
     * This ensures proper serialization of BigDecimal costs and LocalDateTime fields.
     */
    private Map<String, Object> eventToMap(EventDTO event) {
        Map<String, Object> eventData = new java.util.HashMap<>();
        eventData.put("id", event.getId());
        eventData.put("organizerId", event.getOrganizerId());
        eventData.put("organizerName", event.getOrganizerName() != null ? event.getOrganizerName() : "");
        eventData.put("campusId", event.getCampusId());
        eventData.put("campusName", event.getCampusName() != null ? event.getCampusName() : "");
        eventData.put("capacity", event.getCapacity());
        eventData.put("description", event.getDescription() != null ? event.getDescription() : "");
        eventData.put("startTime", event.getStartTime() != null ? event.getStartTime().toString() : "");
        eventData.put("endTime", event.getEndTime() != null ? event.getEndTime().toString() : "");
        eventData.put("ticketsSold", event.getTicketsSold() != null ? event.getTicketsSold() : 0L);
        eventData.put("availableCapacity", event.getAvailableCapacity() != null ? event.getAvailableCapacity() : 0);
        eventData.put("tags", event.getTags() != null ? event.getTags() : new java.util.ArrayList<>());
        
        // Convert costs to simple maps with numeric cost values
        List<Map<String, Object>> costsData = new java.util.ArrayList<>();
        if (event.getCosts() != null) {
            for (var cost : event.getCosts()) {
                Map<String, Object> costMap = new java.util.HashMap<>();
                costMap.put("type", cost.getType());
                costMap.put("cost", cost.getCost() != null ? cost.getCost().doubleValue() : 0.0);
                costsData.add(costMap);
            }
        }
        eventData.put("costs", costsData);
        
        return eventData;
    }
    
    /**
     * Utility to safely convert to Integer.
     */
    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
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
