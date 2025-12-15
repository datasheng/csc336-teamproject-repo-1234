package com.campusevents.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for executing raw SQL queries with prepared statements.
 * 
 * This class provides a simple interface for database operations without using
 * any ORM framework. All queries use parameterized prepared statements to
 * prevent SQL injection attacks.
 * 
 * USAGE EXAMPLES:
 * 
 * 1. SELECT query (returns multiple rows):
 *    List<Map<String, Object>> users = sqlExecutor.executeQuery(
 *        "SELECT * FROM \"user\" WHERE campus_id = ?",
 *        new Object[]{campusId}
 *    );
 * 
 * 2. UPDATE/DELETE query (returns rows affected):
 *    int rowsUpdated = sqlExecutor.executeUpdate(
 *        "UPDATE \"user\" SET email = ? WHERE id = ?",
 *        new Object[]{newEmail, userId}
 *    );
 * 
 * 3. INSERT query (returns generated ID):
 *    Long newId = sqlExecutor.executeInsert(
 *        "INSERT INTO organization (name, description) VALUES (?, ?)",
 *        new Object[]{"Chess Club", "A club for chess enthusiasts"}
 *    );
 * 
 * IMPORTANT NOTES:
 * - Always use parameterized queries (?) instead of string concatenation
 * - PostgreSQL table "user" requires quotes due to reserved keyword
 * - All exceptions are wrapped in RuntimeException for simplicity
 * - Results are returned as List<Map<String, Object>> for flexibility
 */
@Component
public class SqlExecutor {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor injection of JdbcTemplate.
     * 
     * @param jdbcTemplate The JdbcTemplate bean from DatabaseConfig
     */
    public SqlExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Executes a SELECT query and returns results as a list of maps.
     * 
     * Each map represents a row where:
     * - Key: column name (lowercase)
     * - Value: column value (as Object)
     * 
     * Example:
     *   List<Map<String, Object>> results = executeQuery(
     *       "SELECT id, name, email FROM \"user\" WHERE campus_id = ?",
     *       new Object[]{1}
     *   );
     *   
     *   for (Map<String, Object> row : results) {
     *       Long id = (Long) row.get("id");
     *       String name = (String) row.get("name");
     *   }
     * 
     * @param sql The SQL SELECT query with ? placeholders for parameters
     * @param params Array of parameter values (can be null or empty for no params)
     * @return List of maps, each representing a row; empty list if no results
     * @throws RuntimeException if query execution fails
     */
    public List<Map<String, Object>> executeQuery(String sql, Object[] params) {
        try {
            if (params == null || params.length == 0) {
                return jdbcTemplate.queryForList(sql);
            }
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query: " + sql, e);
        }
    }

    /**
     * Executes an UPDATE, DELETE, or other modification query.
     * 
     * Example:
     *   int rowsAffected = executeUpdate(
     *       "DELETE FROM ticket WHERE user_id = ? AND event_id = ?",
     *       new Object[]{userId, eventId}
     *   );
     *   
     *   if (rowsAffected == 0) {
     *       // No ticket was found to delete
     *   }
     * 
     * @param sql The SQL UPDATE/DELETE query with ? placeholders
     * @param params Array of parameter values
     * @return Number of rows affected by the query
     * @throws RuntimeException if query execution fails
     */
    public int executeUpdate(String sql, Object[] params) {
        try {
            if (params == null || params.length == 0) {
                return jdbcTemplate.update(sql);
            }
            return jdbcTemplate.update(sql, params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute update: " + sql, e);
        }
    }

    /**
     * Executes an INSERT query and returns the generated primary key.
     * 
     * This method is designed for tables with SERIAL/auto-increment primary keys.
     * It returns the generated ID after successful insertion.
     * 
     * Example:
     *   Long eventId = executeInsert(
     *       "INSERT INTO event (organizer_id, campus_id, capacity, description, start_time, end_time) " +
     *       "VALUES (?, ?, ?, ?, ?, ?)",
     *       new Object[]{orgId, campusId, 100, "Annual meetup", startTime, endTime}
     *   );
     *   
     *   System.out.println("Created event with ID: " + eventId);
     * 
     * @param sql The SQL INSERT query with ? placeholders
     * @param params Array of parameter values for the INSERT
     * @return The generated primary key (as Long)
     * @throws RuntimeException if insertion fails or no key is generated
     */
    public Long executeInsert(String sql, Object[] params) {
        try {
            // For PostgreSQL, use RETURNING id for reliable ID retrieval
            String sqlWithReturning = sql;
            if (!sql.toLowerCase().contains("returning")) {
                sqlWithReturning = sql + " RETURNING id";
            }
            
            // Try using queryForMap with RETURNING clause first
            try {
                Map<String, Object> result;
                if (params == null || params.length == 0) {
                    result = jdbcTemplate.queryForMap(sqlWithReturning);
                } else {
                    result = jdbcTemplate.queryForMap(sqlWithReturning, params);
                }
                if (result != null && result.get("id") != null) {
                    return ((Number) result.get("id")).longValue();
                }
            } catch (Exception e) {
                // Fall back to KeyHolder approach
            }
            
            // Fallback: use KeyHolder approach
            KeyHolder keyHolder = new GeneratedKeyHolder();
            final String finalSql = sql.replace(" RETURNING id", ""); // Remove RETURNING for this approach

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(finalSql, Statement.RETURN_GENERATED_KEYS);
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        ps.setObject(i + 1, params[i]);
                    }
                }
                return ps;
            }, keyHolder);

            // Get the generated key
            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null && keys.containsKey("id")) {
                return ((Number) keys.get("id")).longValue();
            }
            
            // Fallback for different key column names
            Number key = keyHolder.getKey();
            return key != null ? key.longValue() : null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to execute insert: " + sql, e);
        }
    }

    /**
     * Executes a query that returns a single value.
     * 
     * Useful for COUNT, SUM, MAX, MIN queries or selecting a single column.
     * 
     * Example:
     *   Long count = executeScalar(
     *       "SELECT COUNT(*) FROM ticket WHERE event_id = ?",
     *       new Object[]{eventId},
     *       Long.class
     *   );
     * 
     * @param sql The SQL query with ? placeholders
     * @param params Array of parameter values
     * @param type The expected return type class
     * @return The single value result, or null if no result
     * @throws RuntimeException if query execution fails
     */
    public <T> T executeScalar(String sql, Object[] params, Class<T> type) {
        try {
            if (params == null || params.length == 0) {
                return jdbcTemplate.queryForObject(sql, type);
            }
            return jdbcTemplate.queryForObject(sql, type, params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute scalar query: " + sql, e);
        }
    }

    /**
     * Executes a query that returns a single row as a map.
     * 
     * Example:
     *   Map<String, Object> user = executeQueryForMap(
     *       "SELECT * FROM \"user\" WHERE email = ?",
     *       new Object[]{"john@example.com"}
     *   );
     *   
     *   if (user != null) {
     *       String name = (String) user.get("first_name");
     *   }
     * 
     * @param sql The SQL query with ? placeholders
     * @param params Array of parameter values
     * @return Map representing the single row, or null if no result
     * @throws RuntimeException if query execution fails or multiple rows returned
     */
    public Map<String, Object> executeQueryForMap(String sql, Object[] params) {
        try {
            List<Map<String, Object>> results = executeQuery(sql, params);
            if (results.isEmpty()) {
                return null;
            }
            if (results.size() > 1) {
                throw new RuntimeException("Query returned multiple rows when single row expected");
            }
            return results.get(0);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("Failed to execute query for single row: " + sql, e);
        }
    }

    /**
     * Checks if any rows exist for the given query.
     * 
     * Example:
     *   boolean emailExists = exists(
     *       "SELECT 1 FROM \"user\" WHERE email = ?",
     *       new Object[]{"john@example.com"}
     *   );
     * 
     * @param sql The SQL query with ? placeholders
     * @param params Array of parameter values
     * @return true if at least one row exists, false otherwise
     */
    public boolean exists(String sql, Object[] params) {
        List<Map<String, Object>> results = executeQuery(sql, params);
        return !results.isEmpty();
    }
}
