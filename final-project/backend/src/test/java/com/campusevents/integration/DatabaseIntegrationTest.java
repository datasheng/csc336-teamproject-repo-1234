package com.campusevents.integration;

import com.campusevents.util.SqlExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database integration tests using H2 in-memory database.
 * Tests actual SQL execution with the SqlExecutor utility.
 */
class DatabaseIntegrationTest {
    
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private SqlExecutor sqlExecutor;
    
    @BeforeEach
    void setUp() {
        // Create H2 in-memory database with PostgreSQL compatibility mode
        // Use unique DB name per test to avoid state leaking between tests
        String uniqueDbName = "testdb_" + UUID.randomUUID().toString().replace("-", "");
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:" + uniqueDbName + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
        ds.setUsername("sa");
        ds.setPassword("");
        
        dataSource = ds;
        jdbcTemplate = new JdbcTemplate(dataSource);
        sqlExecutor = new SqlExecutor(jdbcTemplate);
        
        // Create test schema
        createTestSchema();
        insertTestData();
    }
    
    @AfterEach
    void tearDown() {
        // Drop all tables in reverse order
        jdbcTemplate.execute("DROP TABLE IF EXISTS ticket");
        jdbcTemplate.execute("DROP TABLE IF EXISTS cost");
        jdbcTemplate.execute("DROP TABLE IF EXISTS event");
        jdbcTemplate.execute("DROP TABLE IF EXISTS fee");
        jdbcTemplate.execute("DROP TABLE IF EXISTS org_leadership");
        jdbcTemplate.execute("DROP TABLE IF EXISTS organization");
        jdbcTemplate.execute("DROP TABLE IF EXISTS \"user\"");
        jdbcTemplate.execute("DROP TABLE IF EXISTS campus");
        jdbcTemplate.execute("DROP TABLE IF EXISTS city_table");
    }
    
    private void createTestSchema() {
        // City table (renamed to city_table to avoid reserved word conflicts)
        jdbcTemplate.execute("""
            CREATE TABLE city_table (
                city_name VARCHAR(100) PRIMARY KEY,
                country VARCHAR(100) NOT NULL
            )
            """);
        
        // Campus table
        jdbcTemplate.execute("""
            CREATE TABLE campus (
                id SERIAL PRIMARY KEY,
                name VARCHAR(200) NOT NULL,
                address VARCHAR(300) NOT NULL,
                zip_code VARCHAR(20) NOT NULL,
                city_name VARCHAR(100) NOT NULL,
                FOREIGN KEY (city_name) REFERENCES city_table(city_name)
            )
            """);
        
        // User table
        jdbcTemplate.execute("""
            CREATE TABLE "user" (
                id SERIAL PRIMARY KEY,
                first_name VARCHAR(100) NOT NULL,
                last_name VARCHAR(100) NOT NULL,
                email VARCHAR(255) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                campus_id INTEGER NOT NULL,
                FOREIGN KEY (campus_id) REFERENCES campus(id)
            )
            """);
        
        // Organization table
        jdbcTemplate.execute("""
            CREATE TABLE organization (
                id SERIAL PRIMARY KEY,
                name VARCHAR(200) NOT NULL,
                description TEXT
            )
            """);
        
        // Org leadership table
        jdbcTemplate.execute("""
            CREATE TABLE org_leadership (
                user_id INTEGER NOT NULL,
                org_id INTEGER NOT NULL,
                PRIMARY KEY (user_id, org_id),
                FOREIGN KEY (user_id) REFERENCES "user"(id),
                FOREIGN KEY (org_id) REFERENCES organization(id)
            )
            """);
        
        // Fee table (must be before ticket)
        jdbcTemplate.execute("""
            CREATE TABLE fee (
                id INTEGER PRIMARY KEY,
                start_time TIMESTAMP NOT NULL,
                end_time TIMESTAMP NOT NULL,
                fee_percent FLOAT NOT NULL
            )
            """);
        
        // Event table
        jdbcTemplate.execute("""
            CREATE TABLE event (
                id SERIAL PRIMARY KEY,
                organizer_id INTEGER NOT NULL,
                campus_id INTEGER NOT NULL,
                capacity INTEGER NOT NULL,
                description TEXT,
                start_time TIMESTAMP NOT NULL,
                end_time TIMESTAMP NOT NULL,
                FOREIGN KEY (organizer_id) REFERENCES organization(id),
                FOREIGN KEY (campus_id) REFERENCES campus(id)
            )
            """);
        
        // Cost table
        jdbcTemplate.execute("""
            CREATE TABLE cost (
                type VARCHAR(50) NOT NULL,
                event_id INTEGER NOT NULL,
                cost DECIMAL(10, 2) NOT NULL,
                PRIMARY KEY (type, event_id),
                FOREIGN KEY (event_id) REFERENCES event(id)
            )
            """);
        
        // Ticket table
        jdbcTemplate.execute("""
            CREATE TABLE ticket (
                user_id INTEGER NOT NULL,
                event_id INTEGER NOT NULL,
                type VARCHAR(50) NOT NULL,
                time_period INTEGER NOT NULL,
                PRIMARY KEY (user_id, event_id, type),
                FOREIGN KEY (user_id) REFERENCES "user"(id),
                FOREIGN KEY (event_id) REFERENCES event(id),
                FOREIGN KEY (time_period) REFERENCES fee(id)
            )
            """);
    }
    
    private void insertTestData() {
        // Insert cities
        jdbcTemplate.update("INSERT INTO city_table (city_name, country) VALUES (?, ?)", "Cambridge", "USA");
        jdbcTemplate.update("INSERT INTO city_table (city_name, country) VALUES (?, ?)", "Stanford", "USA");
        
        // Insert campuses
        jdbcTemplate.update(
            "INSERT INTO campus (name, address, zip_code, city_name) VALUES (?, ?, ?, ?)",
            "Harvard University", "Massachusetts Hall", "02138", "Cambridge"
        );
        jdbcTemplate.update(
            "INSERT INTO campus (name, address, zip_code, city_name) VALUES (?, ?, ?, ?)",
            "Stanford University", "450 Serra Mall", "94305", "Stanford"
        );
        
        // Insert users
        jdbcTemplate.update(
            "INSERT INTO \"user\" (first_name, last_name, email, password, campus_id) VALUES (?, ?, ?, ?, ?)",
            "John", "Doe", "john@harvard.edu", "hashedpw", 1
        );
        jdbcTemplate.update(
            "INSERT INTO \"user\" (first_name, last_name, email, password, campus_id) VALUES (?, ?, ?, ?, ?)",
            "Jane", "Smith", "jane@stanford.edu", "hashedpw", 2
        );
        
        // Insert organizations
        jdbcTemplate.update(
            "INSERT INTO organization (name, description) VALUES (?, ?)",
            "Tech Club", "Technology enthusiasts"
        );
        
        // Insert org leadership
        jdbcTemplate.update(
            "INSERT INTO org_leadership (user_id, org_id) VALUES (?, ?)",
            1, 1
        );
        
        // Insert fee periods
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
            "INSERT INTO fee (id, start_time, end_time, fee_percent) VALUES (?, ?, ?, ?)",
            1, Timestamp.valueOf(now.minusDays(30)), Timestamp.valueOf(now.plusDays(30)), 0.05
        );
        
        // Insert events
        jdbcTemplate.update(
            "INSERT INTO event (organizer_id, campus_id, capacity, description, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?)",
            1, 1, 100, "Tech Meetup", 
            Timestamp.valueOf(now.plusDays(7)), Timestamp.valueOf(now.plusDays(7).plusHours(3))
        );
        
        // Insert costs
        jdbcTemplate.update(
            "INSERT INTO cost (type, event_id, cost) VALUES (?, ?, ?)",
            "General", 1, new BigDecimal("25.00")
        );
        jdbcTemplate.update(
            "INSERT INTO cost (type, event_id, cost) VALUES (?, ?, ?)",
            "VIP", 1, new BigDecimal("50.00")
        );
    }
    
    @Nested
    @DisplayName("SqlExecutor Query Tests")
    class SqlExecutorQueryTests {
        
        @Test
        @DisplayName("Should execute query and return results")
        void shouldExecuteQueryAndReturnResults() {
            List<Map<String, Object>> results = sqlExecutor.executeQuery(
                "SELECT * FROM campus ORDER BY id", 
                new Object[]{}
            );
            
            assertEquals(2, results.size());
            assertEquals("Harvard University", results.get(0).get("name"));
            assertEquals("Stanford University", results.get(1).get("name"));
        }
        
        @Test
        @DisplayName("Should execute query with parameters")
        void shouldExecuteQueryWithParameters() {
            List<Map<String, Object>> results = sqlExecutor.executeQuery(
                "SELECT * FROM \"user\" WHERE campus_id = ?",
                new Object[]{1}
            );
            
            assertEquals(1, results.size());
            assertEquals("John", results.get(0).get("first_name"));
        }
        
        @Test
        @DisplayName("Should return empty list for no matches")
        void shouldReturnEmptyListForNoMatches() {
            List<Map<String, Object>> results = sqlExecutor.executeQuery(
                "SELECT * FROM \"user\" WHERE email = ?",
                new Object[]{"nonexistent@example.com"}
            );
            
            assertTrue(results.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("SqlExecutor QueryForMap Tests")
    class SqlExecutorQueryForMapTests {
        
        @Test
        @DisplayName("Should return single row as map")
        void shouldReturnSingleRowAsMap() {
            Map<String, Object> result = sqlExecutor.executeQueryForMap(
                "SELECT * FROM \"user\" WHERE id = ?",
                new Object[]{1}
            );
            
            assertNotNull(result);
            assertEquals("John", result.get("first_name"));
            assertEquals("Doe", result.get("last_name"));
            assertEquals("john@harvard.edu", result.get("email"));
        }
        
        @Test
        @DisplayName("Should return null for no match")
        void shouldReturnNullForNoMatch() {
            Map<String, Object> result = sqlExecutor.executeQueryForMap(
                "SELECT * FROM \"user\" WHERE id = ?",
                new Object[]{999}
            );
            
            assertNull(result);
        }
    }
    
    @Nested
    @DisplayName("SqlExecutor Exists Tests")
    class SqlExecutorExistsTests {
        
        @Test
        @DisplayName("Should return true when record exists")
        void shouldReturnTrueWhenRecordExists() {
            boolean exists = sqlExecutor.exists(
                "SELECT 1 FROM \"user\" WHERE email = ?",
                new Object[]{"john@harvard.edu"}
            );
            
            assertTrue(exists);
        }
        
        @Test
        @DisplayName("Should return false when record does not exist")
        void shouldReturnFalseWhenRecordDoesNotExist() {
            boolean exists = sqlExecutor.exists(
                "SELECT 1 FROM \"user\" WHERE email = ?",
                new Object[]{"nonexistent@example.com"}
            );
            
            assertFalse(exists);
        }
        
        @Test
        @DisplayName("Should check org leadership correctly")
        void shouldCheckOrgLeadershipCorrectly() {
            boolean isLeader = sqlExecutor.exists(
                "SELECT 1 FROM org_leadership WHERE user_id = ? AND org_id = ?",
                new Object[]{1, 1}
            );
            assertTrue(isLeader);
            
            boolean isNotLeader = sqlExecutor.exists(
                "SELECT 1 FROM org_leadership WHERE user_id = ? AND org_id = ?",
                new Object[]{2, 1}
            );
            assertFalse(isNotLeader);
        }
    }
    
    @Nested
    @DisplayName("SqlExecutor Update Tests")
    class SqlExecutorUpdateTests {
        
        @Test
        @DisplayName("Should update record and return affected rows")
        void shouldUpdateRecordAndReturnAffectedRows() {
            int affected = sqlExecutor.executeUpdate(
                "UPDATE \"user\" SET first_name = ? WHERE id = ?",
                new Object[]{"Johnny", 1}
            );
            
            assertEquals(1, affected);
            
            // Verify update
            Map<String, Object> user = sqlExecutor.executeQueryForMap(
                "SELECT first_name FROM \"user\" WHERE id = ?",
                new Object[]{1}
            );
            assertEquals("Johnny", user.get("first_name"));
        }
        
        @Test
        @DisplayName("Should return zero for no matching records")
        void shouldReturnZeroForNoMatchingRecords() {
            int affected = sqlExecutor.executeUpdate(
                "UPDATE \"user\" SET first_name = ? WHERE id = ?",
                new Object[]{"Test", 999}
            );
            
            assertEquals(0, affected);
        }
    }
    
    @Nested
    @DisplayName("SqlExecutor Insert Tests")
    class SqlExecutorInsertTests {
        
        @Test
        @DisplayName("Should insert record and return generated ID")
        void shouldInsertRecordAndReturnGeneratedId() {
            Long newId = sqlExecutor.executeInsert(
                "INSERT INTO organization (name, description) VALUES (?, ?)",
                new Object[]{"New Org", "Description"}
            );
            
            assertNotNull(newId);
            assertTrue(newId > 0);
            
            // Verify insert
            Map<String, Object> org = sqlExecutor.executeQueryForMap(
                "SELECT * FROM organization WHERE id = ?",
                new Object[]{newId}
            );
            assertEquals("New Org", org.get("name"));
        }
    }
    
    @Nested
    @DisplayName("Event and Ticket Flow Tests")
    class EventAndTicketFlowTests {
        
        @Test
        @DisplayName("Should query event with costs")
        void shouldQueryEventWithCosts() {
            List<Map<String, Object>> costs = sqlExecutor.executeQuery(
                "SELECT type, cost FROM cost WHERE event_id = ? ORDER BY type",
                new Object[]{1}
            );
            
            assertEquals(2, costs.size());
            assertEquals("General", costs.get(0).get("type"));
            assertEquals("VIP", costs.get(1).get("type"));
        }
        
        @Test
        @DisplayName("Should insert ticket successfully")
        void shouldInsertTicketSuccessfully() {
            int affected = sqlExecutor.executeUpdate(
                "INSERT INTO ticket (user_id, event_id, type, time_period) VALUES (?, ?, ?, ?)",
                new Object[]{2, 1, "General", 1}
            );
            
            assertEquals(1, affected);
            
            // Verify ticket exists
            boolean exists = sqlExecutor.exists(
                "SELECT 1 FROM ticket WHERE user_id = ? AND event_id = ?",
                new Object[]{2, 1}
            );
            assertTrue(exists);
        }
        
        @Test
        @DisplayName("Should count tickets for event")
        void shouldCountTicketsForEvent() {
            // Insert some tickets
            sqlExecutor.executeUpdate(
                "INSERT INTO ticket (user_id, event_id, type, time_period) VALUES (?, ?, ?, ?)",
                new Object[]{1, 1, "General", 1}
            );
            sqlExecutor.executeUpdate(
                "INSERT INTO ticket (user_id, event_id, type, time_period) VALUES (?, ?, ?, ?)",
                new Object[]{2, 1, "VIP", 1}
            );
            
            Map<String, Object> result = sqlExecutor.executeQueryForMap(
                "SELECT COUNT(*) as count FROM ticket WHERE event_id = ?",
                new Object[]{1}
            );
            
            assertEquals(2L, ((Number) result.get("count")).longValue());
        }
        
        @Test
        @DisplayName("Should calculate remaining capacity")
        void shouldCalculateRemainingCapacity() {
            // Insert some tickets
            sqlExecutor.executeUpdate(
                "INSERT INTO ticket (user_id, event_id, type, time_period) VALUES (?, ?, ?, ?)",
                new Object[]{1, 1, "General", 1}
            );
            
            // Get capacity and ticket count
            Map<String, Object> capacityResult = sqlExecutor.executeQueryForMap(
                "SELECT capacity FROM event WHERE id = ?",
                new Object[]{1}
            );
            int capacity = ((Number) capacityResult.get("capacity")).intValue();
            
            Map<String, Object> countResult = sqlExecutor.executeQueryForMap(
                "SELECT COUNT(*) as count FROM ticket WHERE event_id = ?",
                new Object[]{1}
            );
            long ticketCount = ((Number) countResult.get("count")).longValue();
            
            int remaining = capacity - (int) ticketCount;
            
            assertEquals(100, capacity);
            assertEquals(1, ticketCount);
            assertEquals(99, remaining);
        }
    }
    
    @Nested
    @DisplayName("Analytics Query Tests")
    class AnalyticsQueryTests {
        
        @Test
        @DisplayName("Should calculate revenue by ticket type")
        void shouldCalculateRevenueByTicketType() {
            // Insert tickets
            sqlExecutor.executeUpdate(
                "INSERT INTO ticket (user_id, event_id, type, time_period) VALUES (?, ?, ?, ?)",
                new Object[]{1, 1, "General", 1}
            );
            sqlExecutor.executeUpdate(
                "INSERT INTO ticket (user_id, event_id, type, time_period) VALUES (?, ?, ?, ?)",
                new Object[]{2, 1, "VIP", 1}
            );
            
            List<Map<String, Object>> analytics = sqlExecutor.executeQuery(
                "SELECT t.type, COUNT(*) as count, SUM(c.cost) as revenue " +
                "FROM ticket t JOIN cost c ON t.event_id = c.event_id AND t.type = c.type " +
                "WHERE t.event_id = ? GROUP BY t.type ORDER BY t.type",
                new Object[]{1}
            );
            
            assertEquals(2, analytics.size());
            
            // General ticket
            assertEquals("General", analytics.get(0).get("type"));
            assertEquals(1L, ((Number) analytics.get(0).get("count")).longValue());
            assertEquals(new BigDecimal("25.00"), analytics.get(0).get("revenue"));
            
            // VIP ticket
            assertEquals("VIP", analytics.get(1).get("type"));
            assertEquals(1L, ((Number) analytics.get(1).get("count")).longValue());
            assertEquals(new BigDecimal("50.00"), analytics.get(1).get("revenue"));
        }
        
        @Test
        @DisplayName("Should return empty analytics for event with no tickets")
        void shouldReturnEmptyAnalyticsForNoTickets() {
            List<Map<String, Object>> analytics = sqlExecutor.executeQuery(
                "SELECT t.type, COUNT(*) as count, SUM(c.cost) as revenue " +
                "FROM ticket t JOIN cost c ON t.event_id = c.event_id AND t.type = c.type " +
                "WHERE t.event_id = ? GROUP BY t.type",
                new Object[]{999}
            );
            
            assertTrue(analytics.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Complex Join Tests")
    class ComplexJoinTests {
        
        @Test
        @DisplayName("Should join event with organization and campus")
        void shouldJoinEventWithOrgAndCampus() {
            List<Map<String, Object>> results = sqlExecutor.executeQuery(
                "SELECT e.id, e.description, o.name as organizer_name, c.name as campus_name " +
                "FROM event e " +
                "JOIN organization o ON e.organizer_id = o.id " +
                "JOIN campus c ON e.campus_id = c.id " +
                "WHERE e.id = ?",
                new Object[]{1}
            );
            
            assertEquals(1, results.size());
            assertEquals("Tech Meetup", results.get(0).get("description"));
            assertEquals("Tech Club", results.get(0).get("organizer_name"));
            assertEquals("Harvard University", results.get(0).get("campus_name"));
        }
        
        @Test
        @DisplayName("Should get user tickets with event details")
        void shouldGetUserTicketsWithEventDetails() {
            // Insert a ticket
            sqlExecutor.executeUpdate(
                "INSERT INTO ticket (user_id, event_id, type, time_period) VALUES (?, ?, ?, ?)",
                new Object[]{1, 1, "General", 1}
            );
            
            List<Map<String, Object>> tickets = sqlExecutor.executeQuery(
                "SELECT t.type, e.description, e.start_time, e.end_time, o.name as organizer_name, c.cost " +
                "FROM ticket t " +
                "JOIN event e ON t.event_id = e.id " +
                "JOIN organization o ON e.organizer_id = o.id " +
                "JOIN cost c ON t.event_id = c.event_id AND t.type = c.type " +
                "WHERE t.user_id = ?",
                new Object[]{1}
            );
            
            assertEquals(1, tickets.size());
            assertEquals("General", tickets.get(0).get("type"));
            assertEquals("Tech Meetup", tickets.get(0).get("description"));
            assertEquals("Tech Club", tickets.get(0).get("organizer_name"));
            assertEquals(new BigDecimal("25.00"), tickets.get(0).get("cost"));
        }
    }
}
