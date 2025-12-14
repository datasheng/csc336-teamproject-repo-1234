package com.campusevents.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebSocketSessionManager.
 */
class WebSocketSessionManagerTest {
    
    private WebSocketSessionManager sessionManager;
    
    @BeforeEach
    void setUp() {
        sessionManager = new WebSocketSessionManager();
    }
    
    @Nested
    @DisplayName("Session Registration Tests")
    class SessionRegistrationTests {
        
        @Test
        @DisplayName("Should register a new session")
        void shouldRegisterNewSession() {
            sessionManager.registerSession("session-1");
            
            assertTrue(sessionManager.isSessionActive("session-1"));
            assertEquals(1, sessionManager.getActiveSessionCount());
        }
        
        @Test
        @DisplayName("Should register multiple sessions")
        void shouldRegisterMultipleSessions() {
            sessionManager.registerSession("session-1");
            sessionManager.registerSession("session-2");
            sessionManager.registerSession("session-3");
            
            assertEquals(3, sessionManager.getActiveSessionCount());
        }
        
        @Test
        @DisplayName("Should unregister a session")
        void shouldUnregisterSession() {
            sessionManager.registerSession("session-1");
            sessionManager.registerSession("session-2");
            
            sessionManager.unregisterSession("session-1");
            
            assertFalse(sessionManager.isSessionActive("session-1"));
            assertTrue(sessionManager.isSessionActive("session-2"));
            assertEquals(1, sessionManager.getActiveSessionCount());
        }
        
        @Test
        @DisplayName("Should handle duplicate session registration")
        void shouldHandleDuplicateRegistration() {
            sessionManager.registerSession("session-1");
            sessionManager.registerSession("session-1");
            
            assertEquals(1, sessionManager.getActiveSessionCount());
        }
        
        @Test
        @DisplayName("Should handle unregistering non-existent session")
        void shouldHandleUnregisteringNonExistent() {
            assertDoesNotThrow(() -> sessionManager.unregisterSession("non-existent"));
        }
    }
    
    @Nested
    @DisplayName("User Session Tests")
    class UserSessionTests {
        
        @Test
        @DisplayName("Should associate user with session")
        void shouldAssociateUserWithSession() {
            sessionManager.registerUserSession("session-1", 42L);
            
            assertEquals(42L, sessionManager.getUserIdForSession("session-1"));
            assertTrue(sessionManager.isSessionActive("session-1"));
        }
        
        @Test
        @DisplayName("Should track multiple sessions for same user")
        void shouldTrackMultipleSessionsForSameUser() {
            sessionManager.registerUserSession("session-1", 42L);
            sessionManager.registerUserSession("session-2", 42L);
            sessionManager.registerUserSession("session-3", 42L);
            
            Set<String> userSessions = sessionManager.getSessionsForUser(42L);
            assertEquals(3, userSessions.size());
            assertTrue(userSessions.contains("session-1"));
            assertTrue(userSessions.contains("session-2"));
            assertTrue(userSessions.contains("session-3"));
        }
        
        @Test
        @DisplayName("Should clean up user mapping on unregister")
        void shouldCleanUpUserMappingOnUnregister() {
            sessionManager.registerUserSession("session-1", 42L);
            sessionManager.registerUserSession("session-2", 42L);
            
            sessionManager.unregisterSession("session-1");
            
            assertNull(sessionManager.getUserIdForSession("session-1"));
            assertEquals(42L, sessionManager.getUserIdForSession("session-2"));
            
            Set<String> userSessions = sessionManager.getSessionsForUser(42L);
            assertEquals(1, userSessions.size());
            assertFalse(userSessions.contains("session-1"));
        }
        
        @Test
        @DisplayName("Should remove user entry when all sessions disconnect")
        void shouldRemoveUserEntryWhenAllSessionsDisconnect() {
            sessionManager.registerUserSession("session-1", 42L);
            
            sessionManager.unregisterSession("session-1");
            
            Set<String> userSessions = sessionManager.getSessionsForUser(42L);
            assertTrue(userSessions.isEmpty());
            assertEquals(0, sessionManager.getActiveUserCount());
        }
        
        @Test
        @DisplayName("Should count unique authenticated users")
        void shouldCountUniqueAuthenticatedUsers() {
            sessionManager.registerUserSession("session-1", 1L);
            sessionManager.registerUserSession("session-2", 1L);
            sessionManager.registerUserSession("session-3", 2L);
            sessionManager.registerSession("session-4"); // anonymous
            
            assertEquals(2, sessionManager.getActiveUserCount());
            assertEquals(4, sessionManager.getActiveSessionCount());
        }
    }
    
    @Nested
    @DisplayName("Event Subscription Tests")
    class EventSubscriptionTests {
        
        @Test
        @DisplayName("Should subscribe session to event")
        void shouldSubscribeSessionToEvent() {
            sessionManager.registerSession("session-1");
            sessionManager.subscribeToEvent("session-1", 100L);
            
            Set<String> subscribers = sessionManager.getSessionsSubscribedToEvent(100L);
            assertTrue(subscribers.contains("session-1"));
        }
        
        @Test
        @DisplayName("Should track multiple event subscriptions")
        void shouldTrackMultipleEventSubscriptions() {
            sessionManager.registerSession("session-1");
            sessionManager.subscribeToEvent("session-1", 100L);
            sessionManager.subscribeToEvent("session-1", 200L);
            sessionManager.subscribeToEvent("session-1", 300L);
            
            assertTrue(sessionManager.getSessionsSubscribedToEvent(100L).contains("session-1"));
            assertTrue(sessionManager.getSessionsSubscribedToEvent(200L).contains("session-1"));
            assertTrue(sessionManager.getSessionsSubscribedToEvent(300L).contains("session-1"));
        }
        
        @Test
        @DisplayName("Should track multiple sessions subscribed to same event")
        void shouldTrackMultipleSessionsForSameEvent() {
            sessionManager.registerSession("session-1");
            sessionManager.registerSession("session-2");
            sessionManager.registerSession("session-3");
            
            sessionManager.subscribeToEvent("session-1", 100L);
            sessionManager.subscribeToEvent("session-2", 100L);
            sessionManager.subscribeToEvent("session-3", 100L);
            
            Set<String> subscribers = sessionManager.getSessionsSubscribedToEvent(100L);
            assertEquals(3, subscribers.size());
        }
        
        @Test
        @DisplayName("Should unsubscribe from event")
        void shouldUnsubscribeFromEvent() {
            sessionManager.registerSession("session-1");
            sessionManager.subscribeToEvent("session-1", 100L);
            sessionManager.unsubscribeFromEvent("session-1", 100L);
            
            Set<String> subscribers = sessionManager.getSessionsSubscribedToEvent(100L);
            assertFalse(subscribers.contains("session-1"));
        }
        
        @Test
        @DisplayName("Should clean up subscriptions on unregister")
        void shouldCleanUpSubscriptionsOnUnregister() {
            sessionManager.registerSession("session-1");
            sessionManager.subscribeToEvent("session-1", 100L);
            sessionManager.subscribeToCampus("session-1", 1L);
            
            sessionManager.unregisterSession("session-1");
            
            assertTrue(sessionManager.getSessionsSubscribedToEvent(100L).isEmpty());
            assertTrue(sessionManager.getSessionsSubscribedToCampus(1L).isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Campus Subscription Tests")
    class CampusSubscriptionTests {
        
        @Test
        @DisplayName("Should subscribe session to campus")
        void shouldSubscribeSessionToCampus() {
            sessionManager.registerSession("session-1");
            sessionManager.subscribeToCampus("session-1", 1L);
            
            Set<String> subscribers = sessionManager.getSessionsSubscribedToCampus(1L);
            assertTrue(subscribers.contains("session-1"));
        }
        
        @Test
        @DisplayName("Should track multiple campus subscriptions")
        void shouldTrackMultipleCampusSubscriptions() {
            sessionManager.registerSession("session-1");
            sessionManager.registerSession("session-2");
            
            sessionManager.subscribeToCampus("session-1", 1L);
            sessionManager.subscribeToCampus("session-2", 1L);
            
            Set<String> subscribers = sessionManager.getSessionsSubscribedToCampus(1L);
            assertEquals(2, subscribers.size());
        }
    }
    
    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {
        
        @Test
        @DisplayName("Should handle concurrent session registration")
        void shouldHandleConcurrentRegistration() throws InterruptedException {
            int threadCount = 10;
            int sessionsPerThread = 100;
            Thread[] threads = new Thread[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < sessionsPerThread; j++) {
                        sessionManager.registerSession("session-" + threadIndex + "-" + j);
                    }
                });
            }
            
            for (Thread thread : threads) {
                thread.start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            assertEquals(threadCount * sessionsPerThread, sessionManager.getActiveSessionCount());
        }
        
        @Test
        @DisplayName("Should return immutable copy of active sessions")
        void shouldReturnImmutableCopyOfActiveSessions() {
            sessionManager.registerSession("session-1");
            
            Set<String> sessions = sessionManager.getActiveSessions();
            
            assertThrows(UnsupportedOperationException.class, () -> sessions.add("session-2"));
        }
    }
}
