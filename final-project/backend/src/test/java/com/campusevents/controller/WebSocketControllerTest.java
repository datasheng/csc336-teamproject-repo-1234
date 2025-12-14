package com.campusevents.controller;

import com.campusevents.dto.WebSocketMessageDTO;
import com.campusevents.dto.WebSocketNotificationDTO;
import com.campusevents.service.PubSubService;
import com.campusevents.service.WebSocketSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WebSocketController.
 */
@ExtendWith(MockitoExtension.class)
class WebSocketControllerTest {
    
    @Mock
    private PubSubService pubSubService;
    
    @Mock
    private WebSocketSessionManager sessionManager;
    
    @Mock
    private SimpMessageHeaderAccessor headerAccessor;
    
    private WebSocketController controller;
    
    @BeforeEach
    void setUp() {
        controller = new WebSocketController(pubSubService, sessionManager);
        when(headerAccessor.getSessionId()).thenReturn("test-session-123");
    }
    
    @Nested
    @DisplayName("Subscribe to Event Tests")
    class SubscribeToEventTests {
        
        @Test
        @DisplayName("Should subscribe session to event and return confirmation")
        void shouldSubscribeToEvent() {
            WebSocketNotificationDTO result = controller.subscribeToEvent(100L, headerAccessor);
            
            verify(sessionManager).subscribeToEvent("test-session-123", 100L);
            
            assertNotNull(result);
            assertEquals("SUBSCRIPTION_CONFIRMED", result.getType());
            assertNotNull(result.getData());
            assertEquals("event", result.getData().get("subscriptionType"));
            assertEquals(100L, result.getData().get("targetId"));
        }
    }
    
    @Nested
    @DisplayName("Subscribe to Campus Tests")
    class SubscribeToCampusTests {
        
        @Test
        @DisplayName("Should subscribe session to campus and return confirmation")
        void shouldSubscribeToCampus() {
            WebSocketNotificationDTO result = controller.subscribeToCampus(1L, headerAccessor);
            
            verify(sessionManager).subscribeToCampus("test-session-123", 1L);
            
            assertNotNull(result);
            assertEquals("SUBSCRIPTION_CONFIRMED", result.getType());
            assertNotNull(result.getData());
            assertEquals("campus", result.getData().get("subscriptionType"));
            assertEquals(1L, result.getData().get("targetId"));
        }
    }
    
    @Nested
    @DisplayName("Publish Message Tests")
    class PublishMessageTests {
        
        @Test
        @DisplayName("Should publish message to Pub/Sub")
        void shouldPublishMessage() {
            WebSocketMessageDTO message = new WebSocketMessageDTO();
            message.setType("CUSTOM_EVENT");
            message.setEventId(100L);
            message.setCampusId(1L);
            
            when(pubSubService.publishMessage(any())).thenReturn("msg-id-123");
            
            WebSocketNotificationDTO result = controller.publishMessage(message, headerAccessor);
            
            verify(pubSubService).publishMessage(any(Map.class));
            
            assertNotNull(result);
            assertEquals("PUBLISH_CONFIRMED", result.getType());
            assertEquals("msg-id-123", result.getData().get("messageId"));
        }
        
        @Test
        @DisplayName("Should add user context when authenticated")
        void shouldAddUserContextWhenAuthenticated() {
            WebSocketMessageDTO message = new WebSocketMessageDTO();
            message.setType("CUSTOM_EVENT");
            message.setEventId(100L);
            
            when(sessionManager.getUserIdForSession("test-session-123")).thenReturn(42L);
            when(pubSubService.publishMessage(any())).thenReturn("msg-id-456");
            
            controller.publishMessage(message, headerAccessor);
            
            assertEquals(42L, message.getUserId());
        }
        
        @Test
        @DisplayName("Should handle disabled Pub/Sub")
        void shouldHandleDisabledPubSub() {
            WebSocketMessageDTO message = new WebSocketMessageDTO();
            message.setType("CUSTOM_EVENT");
            
            when(pubSubService.publishMessage(any())).thenReturn(null);
            
            WebSocketNotificationDTO result = controller.publishMessage(message, headerAccessor);
            
            assertEquals("PUBLISH_CONFIRMED", result.getType());
            assertEquals("disabled", result.getData().get("messageId"));
        }
        
        @Test
        @DisplayName("Should return error on publish failure")
        void shouldReturnErrorOnPublishFailure() {
            WebSocketMessageDTO message = new WebSocketMessageDTO();
            message.setType("CUSTOM_EVENT");
            
            when(pubSubService.publishMessage(any()))
                .thenThrow(new RuntimeException("Pub/Sub error"));
            
            WebSocketNotificationDTO result = controller.publishMessage(message, headerAccessor);
            
            assertEquals("ERROR", result.getType());
            assertTrue(result.getMessage().contains("Pub/Sub error"));
        }
    }
    
    @Nested
    @DisplayName("Ping Tests")
    class PingTests {
        
        @Test
        @DisplayName("Should respond with pong")
        void shouldRespondWithPong() {
            when(sessionManager.getActiveSessionCount()).thenReturn(5);
            
            WebSocketNotificationDTO result = controller.ping(headerAccessor);
            
            assertEquals("PONG", result.getType());
            assertEquals("test-session-123", result.getData().get("sessionId"));
            assertEquals(5, result.getData().get("activeConnections"));
        }
    }
    
    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {
        
        @Test
        @DisplayName("Should authenticate session with user ID")
        void shouldAuthenticateSession() {
            WebSocketMessageDTO message = new WebSocketMessageDTO();
            message.setUserId(42L);
            
            WebSocketNotificationDTO result = controller.authenticate(message, headerAccessor);
            
            verify(sessionManager).registerUserSession("test-session-123", 42L);
            
            assertEquals("AUTH_CONFIRMED", result.getType());
            assertEquals(42L, result.getUserId());
        }
        
        @Test
        @DisplayName("Should return error when userId is missing")
        void shouldReturnErrorWhenUserIdMissing() {
            WebSocketMessageDTO message = new WebSocketMessageDTO();
            // userId not set
            
            WebSocketNotificationDTO result = controller.authenticate(message, headerAccessor);
            
            verify(sessionManager, never()).registerUserSession(any(), any());
            
            assertEquals("ERROR", result.getType());
            assertTrue(result.getMessage().contains("userId is required"));
        }
    }
    
    @Nested
    @DisplayName("Status Tests")
    class StatusTests {
        
        @Test
        @DisplayName("Should return status for authenticated user")
        void shouldReturnStatusForAuthenticatedUser() {
            when(sessionManager.getUserIdForSession("test-session-123")).thenReturn(42L);
            when(sessionManager.getActiveSessionCount()).thenReturn(10);
            when(sessionManager.getActiveUserCount()).thenReturn(5);
            
            WebSocketNotificationDTO result = controller.getStatus(headerAccessor);
            
            assertEquals("STATUS", result.getType());
            assertEquals(42L, result.getUserId());
            assertEquals(true, result.getData().get("authenticated"));
            assertEquals(10, result.getData().get("activeConnections"));
            assertEquals(5, result.getData().get("activeUsers"));
        }
        
        @Test
        @DisplayName("Should return status for anonymous user")
        void shouldReturnStatusForAnonymousUser() {
            when(sessionManager.getUserIdForSession("test-session-123")).thenReturn(null);
            when(sessionManager.getActiveSessionCount()).thenReturn(1);
            when(sessionManager.getActiveUserCount()).thenReturn(0);
            
            WebSocketNotificationDTO result = controller.getStatus(headerAccessor);
            
            assertEquals("STATUS", result.getType());
            assertNull(result.getUserId());
            assertEquals(false, result.getData().get("authenticated"));
        }
    }
}
