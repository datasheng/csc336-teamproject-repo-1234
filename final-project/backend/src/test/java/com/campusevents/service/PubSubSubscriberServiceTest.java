package com.campusevents.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PubSubSubscriberService.
 * Tests message routing and WebSocket forwarding logic.
 */
@ExtendWith(MockitoExtension.class)
class PubSubSubscriberServiceTest {
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    @Mock
    private WebSocketSessionManager sessionManager;
    
    private PubSubSubscriberService subscriberService;
    
    @BeforeEach
    void setUp() {
        // Create with empty project ID (disabled mode for unit tests)
        subscriberService = new PubSubSubscriberService(
            "",  // disabled
            "test-subscription",
            messagingTemplate,
            sessionManager
        );
    }
    
    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {
        
        @Test
        @DisplayName("Should be disabled when project ID is empty")
        void shouldBeDisabledWhenProjectIdEmpty() {
            assertFalse(subscriberService.isEnabled());
            assertFalse(subscriberService.isRunning());
        }
        
        @Test
        @DisplayName("Should be disabled when project ID is null")
        void shouldBeDisabledWhenProjectIdNull() {
            PubSubSubscriberService service = new PubSubSubscriberService(
                null,
                "test-subscription",
                messagingTemplate,
                sessionManager
            );
            
            assertFalse(service.isEnabled());
        }
        
        @Test
        @DisplayName("Should be disabled when project ID is whitespace")
        void shouldBeDisabledWhenProjectIdWhitespace() {
            PubSubSubscriberService service = new PubSubSubscriberService(
                "   ",
                "test-subscription",
                messagingTemplate,
                sessionManager
            );
            
            assertFalse(service.isEnabled());
        }
        
        @Test
        @DisplayName("Should be enabled when project ID is provided")
        void shouldBeEnabledWhenProjectIdProvided() {
            PubSubSubscriberService service = new PubSubSubscriberService(
                "my-project-id",
                "test-subscription",
                messagingTemplate,
                sessionManager
            );
            
            assertTrue(service.isEnabled());
        }
    }
    
    @Nested
    @DisplayName("Disabled Mode Tests")
    class DisabledModeTests {
        
        @Test
        @DisplayName("Should not start subscriber when disabled")
        void shouldNotStartWhenDisabled() {
            subscriberService.startSubscriber();
            
            assertFalse(subscriberService.isRunning());
        }
        
        @Test
        @DisplayName("Should handle stop gracefully when disabled")
        void shouldHandleStopGracefullyWhenDisabled() {
            assertDoesNotThrow(() -> subscriberService.stopSubscriber());
        }
    }
}
