package com.campusevents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Main application class for the Campus Events Platform.
 * 
 * This application uses Spring Boot with raw SQL (NO ORM).
 * All database operations use JdbcTemplate with prepared statements.
 */
@SpringBootApplication
public class CampusEventsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusEventsApplication.class, args);
    }

    /**
     * Health check controller for the application.
     * Provides a simple endpoint to verify the service is running.
     */
    @RestController
    @RequestMapping("/api")
    public static class HealthController {

        /**
         * Health check endpoint.
         * 
         * @return JSON response with status "ok"
         */
        @GetMapping("/health")
        public ResponseEntity<Map<String, String>> healthCheck() {
            return ResponseEntity.ok(Map.of("status", "ok"));
        }
    }
}
