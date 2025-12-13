package com.campusevents.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PubSubService.
 * Tests message publishing functionality.
 */
class PubSubServiceTest {
    
    private PubSubService pubSubService;
    
    @Nested
    @DisplayName("Disabled PubSub Tests")
    class DisabledPubSubTests {
        
        @BeforeEach
        void setUp() {
            // Create service with empty project ID (disabled)
            pubSubService = new PubSubService("", "event-updates");
        }
        
        @Test
        @DisplayName("Should return null when Pub/Sub is disabled")
        void shouldReturnNullWhenDisabled() {
            String result = pubSubService.publishMessage(Map.of("type", "TEST"));
            assertNull(result);
        }
        
        @Test
        @DisplayName("Should not throw when publishing event created with disabled Pub/Sub")
        void shouldNotThrowWhenPublishingEventCreatedDisabled() {
            assertDoesNotThrow(() -> 
                pubSubService.publishEventCreated(1L, 2L, 3L)
            );
        }
        
        @Test
        @DisplayName("Should not throw when publishing event updated with disabled Pub/Sub")
        void shouldNotThrowWhenPublishingEventUpdatedDisabled() {
            assertDoesNotThrow(() -> 
                pubSubService.publishEventUpdated(1L)
            );
        }
    }
    
    @Nested
    @DisplayName("Null Project ID Tests")
    class NullProjectIdTests {
        
        @BeforeEach
        void setUp() {
            // Create service with null project ID (disabled)
            pubSubService = new PubSubService(null, "event-updates");
        }
        
        @Test
        @DisplayName("Should handle null project ID gracefully")
        void shouldHandleNullProjectIdGracefully() {
            String result = pubSubService.publishMessage(Map.of("type", "TEST"));
            assertNull(result);
        }
    }
}
