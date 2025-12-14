package com.campusevents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
}
