package com.campusevents;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Application context tests.
 * 
 * Verifies that the Spring Boot application context loads successfully
 * and all required beans are available.
 */
@SpringBootTest
class CampusEventsApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    @DisplayName("Main application class should be in context")
    void mainApplicationShouldBeInContext() {
        CampusEventsApplication app = applicationContext.getBean(CampusEventsApplication.class);
        assertNotNull(app);
    }

    @Test
    @DisplayName("JdbcTemplate bean should be available")
    void jdbcTemplateShouldBeAvailable() {
        assertTrue(applicationContext.containsBean("jdbcTemplate"));
    }

    @Test
    @DisplayName("DataSource bean should be available")
    void dataSourceShouldBeAvailable() {
        assertTrue(applicationContext.containsBean("dataSource"));
    }

    @Test
    @DisplayName("SqlExecutor bean should be available")
    void sqlExecutorShouldBeAvailable() {
        assertTrue(applicationContext.containsBean("sqlExecutor"));
    }
}
