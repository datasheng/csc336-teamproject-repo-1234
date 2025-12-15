package com.campusevents.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for publishing messages to Google Cloud Pub/Sub.
 * 
 * Handles publishing event notifications for:
 * - EVENT_CREATED: When a new event is created
 * - EVENT_UPDATED: When an event is updated
 */
@Service
public class PubSubService {
    
    private static final Logger logger = LoggerFactory.getLogger(PubSubService.class);
    
    private final String projectId;
    private final String topicId;
    private final ObjectMapper objectMapper;
    private final Map<String, Publisher> publishers;
    private final boolean enabled;
    
    public PubSubService(
            @Value("${app.gcp.project-id:}") String projectId,
            @Value("${app.pubsub.topic:event-updates}") String topicId) {
        this.projectId = projectId;
        this.topicId = topicId;
        this.objectMapper = new ObjectMapper();
        this.publishers = new ConcurrentHashMap<>();
        this.enabled = projectId != null && !projectId.isBlank();
        
        if (!enabled) {
            logger.warn("Pub/Sub is disabled - GCP_PROJECT_ID not configured");
        }
    }
    
    /**
     * Publish a message to the configured Pub/Sub topic.
     * 
     * @param message The message object to publish (will be serialized to JSON)
     * @return The message ID if successful, null if Pub/Sub is disabled
     */
    public String publishMessage(Object message) {
        if (!enabled) {
            logger.debug("Pub/Sub disabled, skipping message: {}", message);
            return null;
        }
        
        try {
            Publisher publisher = getOrCreatePublisher();
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            ByteString data = ByteString.copyFromUtf8(jsonMessage);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(data)
                    .build();
            
            ApiFuture<String> future = publisher.publish(pubsubMessage);
            String messageId = future.get();
            
            logger.info("Published message with ID: {} to topic: {}", messageId, topicId);
            return messageId;
            
        } catch (Exception e) {
            logger.error("Failed to publish message to Pub/Sub", e);
            throw new RuntimeException("Failed to publish message to Pub/Sub", e);
        }
    }
    
    /**
     * Publish an event creation notification.
     * 
     * @param eventId The ID of the created event
     * @param organizerId The ID of the organizing organization
     * @param campusId The ID of the campus
     */
    public void publishEventCreated(Long eventId, Long organizerId, Long campusId) {
        Map<String, Object> message = Map.of(
            "type", "EVENT_CREATED",
            "eventId", eventId,
            "organizerId", organizerId,
            "campusId", campusId
        );
        publishMessage(message);
    }
    
    /**
     * Publish an event update notification.
     * 
     * @param eventId The ID of the updated event
     */
    public void publishEventUpdated(Long eventId) {
        Map<String, Object> message = Map.of(
            "type", "EVENT_UPDATED",
            "eventId", eventId
        );
        publishMessage(message);
    }
    
    /**
     * Publish a ticket purchase notification.
     * 
     * @param eventId The ID of the event
     * @param userId The ID of the user who purchased
     * @param ticketType The type of ticket purchased
     * @param newTicketsSold The new total tickets sold for this event
     * @param remainingCapacity The remaining capacity after purchase
     * @param campusId The campus ID for dashboard updates
     */
    public void publishTicketPurchased(Long eventId, Long userId, String ticketType, 
            Long newTicketsSold, Integer remainingCapacity, Long campusId) {
        Map<String, Object> message = Map.of(
            "type", "TICKET_PURCHASED",
            "eventId", eventId,
            "userId", userId,
            "ticketType", ticketType,
            "ticketsSold", newTicketsSold,
            "remainingCapacity", remainingCapacity,
            "campusId", campusId
        );
        publishMessage(message);
    }
    
    /**
     * Publish an event deletion notification.
     * 
     * @param eventId The ID of the deleted event
     * @param campusId The campus ID for dashboard updates
     */
    public void publishEventDeleted(Long eventId, Long campusId) {
        Map<String, Object> message = Map.of(
            "type", "EVENT_DELETED",
            "eventId", eventId,
            "campusId", campusId
        );
        publishMessage(message);
    }
    
    /**
     * Publish an event cancellation notification.
     * 
     * @param eventId The ID of the cancelled event
     * @param campusId The campus ID for dashboard updates
     */
    public void publishEventCancelled(Long eventId, Long campusId) {
        Map<String, Object> message = Map.of(
            "type", "EVENT_CANCELLED",
            "eventId", eventId,
            "campusId", campusId
        );
        publishMessage(message);
    }
    
    /**
     * Publish an organization update notification.
     * 
     * @param organizationId The ID of the updated organization
     */
    public void publishOrganizationUpdated(Long organizationId) {
        Map<String, Object> message = Map.of(
            "type", "ORGANIZATION_UPDATED",
            "organizationId", organizationId
        );
        publishMessage(message);
    }
    
    /**
     * Publish an analytics update notification.
     * Sent when ticket purchases change analytics data.
     * 
     * @param eventId The ID of the event
     * @param organizerId The ID of the organization (for org dashboard updates)
     */
    public void publishAnalyticsUpdated(Long eventId, Long organizerId) {
        Map<String, Object> message = Map.of(
            "type", "ANALYTICS_UPDATED",
            "eventId", eventId,
            "organizerId", organizerId
        );
        publishMessage(message);
    }
    
    /**
     * Publish event created with organization info for org page updates.
     * 
     * @param eventId The ID of the created event
     * @param organizerId The ID of the organizing organization
     * @param campusId The ID of the campus
     */
    public void publishEventCreatedWithOrg(Long eventId, Long organizerId, Long campusId) {
        Map<String, Object> message = new java.util.HashMap<>();
        message.put("type", "EVENT_CREATED");
        message.put("eventId", eventId);
        message.put("organizerId", organizerId);
        message.put("campusId", campusId);
        publishMessage(message);
    }
    
    /**
     * Publish event updated with organization info.
     * 
     * @param eventId The ID of the updated event
     * @param organizerId The ID of the organizing organization
     */
    public void publishEventUpdatedWithOrg(Long eventId, Long organizerId) {
        Map<String, Object> message = Map.of(
            "type", "EVENT_UPDATED",
            "eventId", eventId,
            "organizerId", organizerId
        );
        publishMessage(message);
    }
    
    /**
     * Publish an event deletion notification with organization info.
     * 
     * @param eventId The ID of the deleted event
     * @param organizerId The ID of the organization
     * @param campusId The campus ID for dashboard updates
     */
    public void publishEventDeletedWithOrg(Long eventId, Long organizerId, Long campusId) {
        Map<String, Object> message = new java.util.HashMap<>();
        message.put("type", "EVENT_DELETED");
        message.put("eventId", eventId);
        message.put("organizerId", organizerId);
        message.put("campusId", campusId);
        publishMessage(message);
    }
    
    /**
     * Publish an event cancellation notification with organization info.
     * 
     * @param eventId The ID of the cancelled event
     * @param organizerId The ID of the organization
     * @param campusId The campus ID for dashboard updates
     */
    public void publishEventCancelledWithOrg(Long eventId, Long organizerId, Long campusId) {
        Map<String, Object> message = new java.util.HashMap<>();
        message.put("type", "EVENT_CANCELLED");
        message.put("eventId", eventId);
        message.put("organizerId", organizerId);
        message.put("campusId", campusId);
        publishMessage(message);
    }
    
    private Publisher getOrCreatePublisher() throws IOException {
        return publishers.computeIfAbsent(topicId, t -> {
            try {
                TopicName topicName = TopicName.of(projectId, topicId);
                return Publisher.newBuilder(topicName).build();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create Pub/Sub publisher", e);
            }
        });
    }
    
    @PreDestroy
    public void shutdown() {
        for (Publisher publisher : publishers.values()) {
            try {
                publisher.shutdown();
                publisher.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while shutting down Pub/Sub publisher");
            }
        }
    }
}
