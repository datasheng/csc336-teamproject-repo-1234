package com.campusevents.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DatabaseConfig to verify datasource and JdbcTemplate configuration.
 */
@SpringBootTest
class DatabaseConfigTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("DataSource bean should be created")
    void dataSourceBeanShouldBeCreated() {
        assertNotNull(dataSource);
    }

    @Test
    @DisplayName("JdbcTemplate bean should be created")
    void jdbcTemplateBeanShouldBeCreated() {
        assertNotNull(jdbcTemplate);
    }

    @Test
    @DisplayName("DataSource should provide valid connections")
    void dataSourceShouldProvideValidConnections() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        }
    }

    @Test
    @DisplayName("JdbcTemplate should execute simple queries")
    void jdbcTemplateShouldExecuteSimpleQueries() {
        // Execute a simple query
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Connection pool should allow multiple connections")
    void connectionPoolShouldAllowMultipleConnections() throws SQLException {
        Connection conn1 = null;
        Connection conn2 = null;
        Connection conn3 = null;
        
        try {
            conn1 = dataSource.getConnection();
            conn2 = dataSource.getConnection();
            conn3 = dataSource.getConnection();
            
            assertNotNull(conn1);
            assertNotNull(conn2);
            assertNotNull(conn3);
            
            // All connections should be valid
            assertFalse(conn1.isClosed());
            assertFalse(conn2.isClosed());
            assertFalse(conn3.isClosed());
        } finally {
            if (conn1 != null) conn1.close();
            if (conn2 != null) conn2.close();
            if (conn3 != null) conn3.close();
        }
    }
}
