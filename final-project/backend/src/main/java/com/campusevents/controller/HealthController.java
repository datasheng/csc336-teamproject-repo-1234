package com.campusevents.controller;

import com.campusevents.service.PubSubSubscriberService;
import com.campusevents.service.WebSocketSessionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for health checks and status endpoints.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    private final WebSocketSessionManager sessionManager;
    private final PubSubSubscriberService subscriberService;
    
    public HealthController(
            WebSocketSessionManager sessionManager,
            PubSubSubscriberService subscriberService) {
        this.sessionManager = sessionManager;
        this.subscriberService = subscriberService;
    }
    
    /**
     * Basic health check endpoint.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "campus-events-backend"
        ));
    }
    
    /**
     * Detailed status including WebSocket and Pub/Sub state.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "websocket", Map.of(
                "activeSessions", sessionManager.getActiveSessionCount(),
                "activeUsers", sessionManager.getActiveUserCount()
            ),
            "pubsub", Map.of(
                "subscriberEnabled", subscriberService.isEnabled(),
                "subscriberRunning", subscriberService.isRunning()
            )
        ));
    }
}
