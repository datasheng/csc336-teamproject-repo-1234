package com.campusevents.controller;

import com.campusevents.service.PubSubSubscriberService;
import com.campusevents.service.WebSocketSessionManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Health Controller.
 * 
 * Tests the /api/health endpoint and related API functionality.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private WebSocketSessionManager sessionManager;
    
    @MockBean
    private PubSubSubscriberService subscriberService;

    @Test
    @DisplayName("GET /api/health should return status UP")
    void healthCheckShouldReturnStatusOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/health should return JSON content type")
    void healthCheckShouldReturnJsonContentType() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Health check should be accessible without authentication")
    void healthCheckShouldBePubliclyAccessible() throws Exception {
        // No auth headers provided - should still work
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("GET /api/health/status should return detailed status")
    void statusCheckShouldReturnDetailedStatus() throws Exception {
        when(subscriberService.isEnabled()).thenReturn(true);
        when(subscriberService.isRunning()).thenReturn(false);
        
        mockMvc.perform(get("/api/health/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.websocket.activeSessions").exists())
                .andExpect(jsonPath("$.websocket.activeUsers").exists())
                .andExpect(jsonPath("$.pubsub.subscriberEnabled").value(true))
                .andExpect(jsonPath("$.pubsub.subscriberRunning").value(false));
    }

    @Test
    @DisplayName("Non-existent endpoint should return 404")
    void nonExistentEndpointShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("OPTIONS request should work for CORS preflight")
    void optionsRequestShouldWorkForCors() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }
}
