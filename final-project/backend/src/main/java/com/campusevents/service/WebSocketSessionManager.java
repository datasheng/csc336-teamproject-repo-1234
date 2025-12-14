package com.campusevents.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Manages WebSocket session state for connected frontend clients.
 * 
 * This service tracks:
 * - All connected session IDs
 * - User-to-session mappings for authenticated users
 * - Subscription preferences (e.g., which events/campuses a client is interested in)
 * 
 * Thread-safe for concurrent access from multiple WebSocket connections.
 */
@Service
public class WebSocketSessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionManager.class);
    
    // All active session IDs
    private final Set<String> activeSessions = new CopyOnWriteArraySet<>();
    
    // Map of user ID to their session IDs (user can have multiple tabs/devices)
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();
    
    // Map of session ID to user ID (for reverse lookup)
    private final Map<String, Long> sessionToUser = new ConcurrentHashMap<>();
    
    // Map of session ID to subscribed event IDs
    private final Map<String, Set<Long>> sessionEventSubscriptions = new ConcurrentHashMap<>();
    
    // Map of session ID to subscribed campus IDs
    private final Map<String, Set<Long>> sessionCampusSubscriptions = new ConcurrentHashMap<>();
    
    /**
     * Register a new WebSocket session.
     * 
     * @param sessionId The WebSocket session ID
     */
    public void registerSession(String sessionId) {
        activeSessions.add(sessionId);
        logger.info("WebSocket session registered: {}. Total active sessions: {}", 
                    sessionId, activeSessions.size());
    }
    
    /**
     * Register a session with an authenticated user.
     * 
     * @param sessionId The WebSocket session ID
     * @param userId The authenticated user's ID
     */
    public void registerUserSession(String sessionId, Long userId) {
        registerSession(sessionId);
        
        sessionToUser.put(sessionId, userId);
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(sessionId);
        
        logger.info("User {} associated with session {}", userId, sessionId);
    }
    
    /**
     * Unregister a WebSocket session (on disconnect).
     * 
     * @param sessionId The WebSocket session ID
     */
    public void unregisterSession(String sessionId) {
        activeSessions.remove(sessionId);
        
        // Clean up user mapping
        Long userId = sessionToUser.remove(sessionId);
        if (userId != null) {
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }
        
        // Clean up subscriptions
        sessionEventSubscriptions.remove(sessionId);
        sessionCampusSubscriptions.remove(sessionId);
        
        logger.info("WebSocket session unregistered: {}. Remaining sessions: {}", 
                    sessionId, activeSessions.size());
    }
    
    /**
     * Subscribe a session to updates for a specific event.
     * 
     * @param sessionId The WebSocket session ID
     * @param eventId The event ID to subscribe to
     */
    public void subscribeToEvent(String sessionId, Long eventId) {
        sessionEventSubscriptions
            .computeIfAbsent(sessionId, k -> new CopyOnWriteArraySet<>())
            .add(eventId);
        logger.debug("Session {} subscribed to event {}", sessionId, eventId);
    }
    
    /**
     * Unsubscribe a session from a specific event.
     * 
     * @param sessionId The WebSocket session ID
     * @param eventId The event ID to unsubscribe from
     */
    public void unsubscribeFromEvent(String sessionId, Long eventId) {
        Set<Long> events = sessionEventSubscriptions.get(sessionId);
        if (events != null) {
            events.remove(eventId);
        }
    }
    
    /**
     * Subscribe a session to updates for a specific campus.
     * 
     * @param sessionId The WebSocket session ID
     * @param campusId The campus ID to subscribe to
     */
    public void subscribeToCampus(String sessionId, Long campusId) {
        sessionCampusSubscriptions
            .computeIfAbsent(sessionId, k -> new CopyOnWriteArraySet<>())
            .add(campusId);
        logger.debug("Session {} subscribed to campus {}", sessionId, campusId);
    }
    
    /**
     * Get all active session IDs.
     * 
     * @return Set of active session IDs
     */
    public Set<String> getActiveSessions() {
        return Set.copyOf(activeSessions);
    }
    
    /**
     * Get all session IDs for a specific user.
     * 
     * @param userId The user ID
     * @return Set of session IDs for the user
     */
    public Set<String> getSessionsForUser(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null ? Set.copyOf(sessions) : Set.of();
    }
    
    /**
     * Get all session IDs subscribed to a specific event.
     * 
     * @param eventId The event ID
     * @return Set of session IDs subscribed to the event
     */
    public Set<String> getSessionsSubscribedToEvent(Long eventId) {
        Set<String> subscribedSessions = new CopyOnWriteArraySet<>();
        sessionEventSubscriptions.forEach((sessionId, events) -> {
            if (events.contains(eventId)) {
                subscribedSessions.add(sessionId);
            }
        });
        return subscribedSessions;
    }
    
    /**
     * Get all session IDs subscribed to a specific campus.
     * 
     * @param campusId The campus ID
     * @return Set of session IDs subscribed to the campus
     */
    public Set<String> getSessionsSubscribedToCampus(Long campusId) {
        Set<String> subscribedSessions = new CopyOnWriteArraySet<>();
        sessionCampusSubscriptions.forEach((sessionId, campuses) -> {
            if (campuses.contains(campusId)) {
                subscribedSessions.add(sessionId);
            }
        });
        return subscribedSessions;
    }
    
    /**
     * Get the user ID for a session.
     * 
     * @param sessionId The WebSocket session ID
     * @return The user ID, or null if not authenticated
     */
    public Long getUserIdForSession(String sessionId) {
        return sessionToUser.get(sessionId);
    }
    
    /**
     * Check if a session is registered.
     * 
     * @param sessionId The WebSocket session ID
     * @return true if the session is active
     */
    public boolean isSessionActive(String sessionId) {
        return activeSessions.contains(sessionId);
    }
    
    /**
     * Get the total number of active sessions.
     * 
     * @return Number of active WebSocket connections
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * Get the number of unique authenticated users.
     * 
     * @return Number of unique users with active sessions
     */
    public int getActiveUserCount() {
        return userSessions.size();
    }
}
