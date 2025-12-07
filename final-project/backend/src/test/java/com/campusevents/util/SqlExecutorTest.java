package com.campusevents.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SqlExecutor utility class.
 * 
 * Tests verify that all SQL operations work correctly with raw SQL
 * and prepared statements (NO ORM).
 */
@SpringBootTest
class SqlExecutorTest {

    @Autowired
    private SqlExecutor sqlExecutor;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Clean up test data before each test
        jdbcTemplate.execute("DELETE FROM organization WHERE name LIKE 'Test%'");
    }

    @Nested
    @DisplayName("executeQuery tests")
    class ExecuteQueryTests {

        @Test
        @DisplayName("Should return all rows when no parameters provided")
        void shouldReturnAllRowsWithNoParams() {
            // Given - seed data exists from schema.sql
            
            // When
            List<Map<String, Object>> results = sqlExecutor.executeQuery(
                "SELECT * FROM city",
                null
            );
            
            // Then
            assertNotNull(results);
            assertFalse(results.isEmpty());
        }

        @Test
        @DisplayName("Should return filtered rows with parameters")
        void shouldReturnFilteredRowsWithParams() {
            // Given
            String cityName = "Test City";
            
            // When
            List<Map<String, Object>> results = sqlExecutor.executeQuery(
                "SELECT * FROM city WHERE city = ?",
                new Object[]{cityName}
            );
            
            // Then
            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(cityName, results.get(0).get("city"));
        }

        @Test
        @DisplayName("Should return empty list when no matches found")
        void shouldReturnEmptyListWhenNoMatches() {
            // When
            List<Map<String, Object>> results = sqlExecutor.executeQuery(
                "SELECT * FROM city WHERE city = ?",
                new Object[]{"NonExistentCity"}
            );
            
            // Then
            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should handle multiple parameters correctly")
        void shouldHandleMultipleParameters() {
            // Given
            String city = "Test City";
            String country = "Test Country";
            
            // When
            List<Map<String, Object>> results = sqlExecutor.executeQuery(
                "SELECT * FROM city WHERE city = ? AND country = ?",
                new Object[]{city, country}
            );
            
            // Then
            assertNotNull(results);
            assertEquals(1, results.size());
        }
    }

    @Nested
    @DisplayName("executeUpdate tests")
    class ExecuteUpdateTests {

        @Test
        @DisplayName("Should return rows affected count on update")
        void shouldReturnRowsAffectedOnUpdate() {
            // Given - insert test data
            jdbcTemplate.execute(
                "INSERT INTO organization (name, description) VALUES ('TestOrg', 'Test Description')"
            );
            
            // When
            int rowsAffected = sqlExecutor.executeUpdate(
                "UPDATE organization SET description = ? WHERE name = ?",
                new Object[]{"Updated Description", "TestOrg"}
            );
            
            // Then
            assertEquals(1, rowsAffected);
            
            // Verify update worked
            List<Map<String, Object>> results = sqlExecutor.executeQuery(
                "SELECT description FROM organization WHERE name = ?",
                new Object[]{"TestOrg"}
            );
            assertEquals("Updated Description", results.get(0).get("description"));
        }

        @Test
        @DisplayName("Should return zero when no rows affected")
        void shouldReturnZeroWhenNoRowsAffected() {
            // When
            int rowsAffected = sqlExecutor.executeUpdate(
                "UPDATE organization SET description = ? WHERE name = ?",
                new Object[]{"New Description", "NonExistentOrg"}
            );
            
            // Then
            assertEquals(0, rowsAffected);
        }

        @Test
        @DisplayName("Should delete rows correctly")
        void shouldDeleteRowsCorrectly() {
            // Given
            jdbcTemplate.execute(
                "INSERT INTO organization (name, description) VALUES ('ToDelete', 'Delete me')"
            );
            
            // Verify it exists
            List<Map<String, Object>> before = sqlExecutor.executeQuery(
                "SELECT * FROM organization WHERE name = ?",
                new Object[]{"ToDelete"}
            );
            assertEquals(1, before.size());
            
            // When
            int rowsAffected = sqlExecutor.executeUpdate(
                "DELETE FROM organization WHERE name = ?",
                new Object[]{"ToDelete"}
            );
            
            // Then
            assertEquals(1, rowsAffected);
            
            // Verify deletion
            List<Map<String, Object>> after = sqlExecutor.executeQuery(
                "SELECT * FROM organization WHERE name = ?",
                new Object[]{"ToDelete"}
            );
            assertTrue(after.isEmpty());
        }
    }

    @Nested
    @DisplayName("executeInsert tests")
    class ExecuteInsertTests {

        @Test
        @DisplayName("Should return generated ID on insert")
        void shouldReturnGeneratedIdOnInsert() {
            // When
            Long generatedId = sqlExecutor.executeInsert(
                "INSERT INTO organization (name, description) VALUES (?, ?)",
                new Object[]{"TestNewOrg", "A new organization"}
            );
            
            // Then
            assertNotNull(generatedId);
            assertTrue(generatedId > 0);
            
            // Verify the insert worked
            List<Map<String, Object>> results = sqlExecutor.executeQuery(
                "SELECT * FROM organization WHERE id = ?",
                new Object[]{generatedId}
            );
            assertEquals(1, results.size());
            assertEquals("TestNewOrg", results.get(0).get("name"));
        }

        @Test
        @DisplayName("Should handle null description on insert")
        void shouldHandleNullDescriptionOnInsert() {
            // When
            Long generatedId = sqlExecutor.executeInsert(
                "INSERT INTO organization (name, description) VALUES (?, ?)",
                new Object[]{"TestNullDescOrg", null}
            );
            
            // Then
            assertNotNull(generatedId);
            
            // Verify
            List<Map<String, Object>> results = sqlExecutor.executeQuery(
                "SELECT description FROM organization WHERE id = ?",
                new Object[]{generatedId}
            );
            assertNull(results.get(0).get("description"));
        }
    }

    @Nested
    @DisplayName("executeScalar tests")
    class ExecuteScalarTests {

        @Test
        @DisplayName("Should return count correctly")
        void shouldReturnCountCorrectly() {
            // Given - seed data exists
            
            // When
            Long count = sqlExecutor.executeScalar(
                "SELECT COUNT(*) FROM city",
                null,
                Long.class
            );
            
            // Then
            assertNotNull(count);
            assertTrue(count >= 1);
        }

        @Test
        @DisplayName("Should return single value correctly")
        void shouldReturnSingleValueCorrectly() {
            // Given
            jdbcTemplate.execute(
                "INSERT INTO organization (name, description) VALUES ('TestScalarOrg', 'Scalar test')"
            );
            
            // When
            String name = sqlExecutor.executeScalar(
                "SELECT name FROM organization WHERE description = ?",
                new Object[]{"Scalar test"},
                String.class
            );
            
            // Then
            assertEquals("TestScalarOrg", name);
        }
    }

    @Nested
    @DisplayName("executeQueryForMap tests")
    class ExecuteQueryForMapTests {

        @Test
        @DisplayName("Should return single row as map")
        void shouldReturnSingleRowAsMap() {
            // Given
            String cityName = "Test City";
            
            // When
            Map<String, Object> result = sqlExecutor.executeQueryForMap(
                "SELECT * FROM city WHERE city = ?",
                new Object[]{cityName}
            );
            
            // Then
            assertNotNull(result);
            assertEquals(cityName, result.get("city"));
            assertEquals("Test Country", result.get("country"));
        }

        @Test
        @DisplayName("Should return null when no row found")
        void shouldReturnNullWhenNoRowFound() {
            // When
            Map<String, Object> result = sqlExecutor.executeQueryForMap(
                "SELECT * FROM city WHERE city = ?",
                new Object[]{"NonExistent"}
            );
            
            // Then
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("exists tests")
    class ExistsTests {

        @Test
        @DisplayName("Should return true when row exists")
        void shouldReturnTrueWhenRowExists() {
            // When
            boolean exists = sqlExecutor.exists(
                "SELECT 1 FROM city WHERE city = ?",
                new Object[]{"Test City"}
            );
            
            // Then
            assertTrue(exists);
        }

        @Test
        @DisplayName("Should return false when row does not exist")
        void shouldReturnFalseWhenRowDoesNotExist() {
            // When
            boolean exists = sqlExecutor.exists(
                "SELECT 1 FROM city WHERE city = ?",
                new Object[]{"NonExistent"}
            );
            
            // Then
            assertFalse(exists);
        }
    }

    @Nested
    @DisplayName("SQL Injection Prevention tests")
    class SqlInjectionPreventionTests {

        @Test
        @DisplayName("Should safely handle SQL injection attempts in parameters")
        void shouldSafelyHandleSqlInjectionAttempts() {
            // Given - a malicious input that would be SQL injection if not parameterized
            String maliciousInput = "'; DROP TABLE city; --";
            
            // When - using parameterized query (safe)
            List<Map<String, Object>> results = sqlExecutor.executeQuery(
                "SELECT * FROM city WHERE city = ?",
                new Object[]{maliciousInput}
            );
            
            // Then - query executes safely and returns empty (no match)
            assertTrue(results.isEmpty());
            
            // Verify table still exists
            List<Map<String, Object>> cityCheck = sqlExecutor.executeQuery(
                "SELECT * FROM city",
                null
            );
            assertFalse(cityCheck.isEmpty(), "City table should still exist");
        }
    }
}
