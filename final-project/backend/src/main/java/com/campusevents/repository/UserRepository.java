package com.campusevents.repository;

import com.campusevents.model.User;
import com.campusevents.util.SqlExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for User database operations using raw SQL.
 * 
 * IMPORTANT: NO ORM is used. All queries are raw SQL with prepared statements.
 * The "user" table name is quoted because it's a reserved keyword in PostgreSQL.
 */
@Repository
public class UserRepository {
    
    private final SqlExecutor sqlExecutor;
    
    public UserRepository(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }
    
    /**
     * Find a user by email address.
     * 
     * @param email The email to search for
     * @return Optional containing the user if found, empty otherwise
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, first_name, last_name, email, password, campus_id, COALESCE(is_admin, FALSE) as is_admin " +
                     "FROM \"user\" WHERE email = ?";
        
        List<Map<String, Object>> results = sqlExecutor.executeQuery(sql, new Object[]{email});
        
        if (results.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(mapRowToUser(results.get(0)));
    }
    
    /**
     * Find a user by ID.
     * 
     * @param id The user ID
     * @return Optional containing the user if found, empty otherwise
     */
    public Optional<User> findById(Long id) {
        String sql = "SELECT id, first_name, last_name, email, password, campus_id, COALESCE(is_admin, FALSE) as is_admin " +
                     "FROM \"user\" WHERE id = ?";
        
        List<Map<String, Object>> results = sqlExecutor.executeQuery(sql, new Object[]{id});
        
        if (results.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(mapRowToUser(results.get(0)));
    }
    
    /**
     * Save a new user to the database.
     * 
     * @param user The user to save (password should already be hashed)
     * @return The saved user with generated ID
     */
    public User save(User user) {
        String sql = "INSERT INTO \"user\" (first_name, last_name, email, password, campus_id, is_admin) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        Long generatedId = sqlExecutor.executeInsert(sql, new Object[]{
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPassword(),
            user.getCampusId(),
            user.getIsAdmin() != null ? user.getIsAdmin() : false
        });
        
        user.setId(generatedId);
        return user;
    }
    
    /**
     * Check if a user exists with the given email.
     * 
     * @param email The email to check
     * @return true if a user with this email exists
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM \"user\" WHERE email = ?";
        return sqlExecutor.exists(sql, new Object[]{email});
    }
    
    /**
     * Update an existing user.
     * 
     * @param user The user with updated fields
     * @return Number of rows affected (should be 1)
     */
    public int update(User user) {
        String sql = "UPDATE \"user\" SET first_name = ?, last_name = ?, email = ?, campus_id = ? " +
                     "WHERE id = ?";
        
        return sqlExecutor.executeUpdate(sql, new Object[]{
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getCampusId(),
            user.getId()
        });
    }
    
    /**
     * Update a user's password.
     * 
     * @param userId The user ID
     * @param hashedPassword The new BCrypt hashed password
     * @return Number of rows affected
     */
    public int updatePassword(Long userId, String hashedPassword) {
        String sql = "UPDATE \"user\" SET password = ? WHERE id = ?";
        return sqlExecutor.executeUpdate(sql, new Object[]{hashedPassword, userId});
    }
    
    /**
     * Delete a user by ID.
     * 
     * @param id The user ID to delete
     * @return Number of rows affected
     */
    public int deleteById(Long id) {
        String sql = "DELETE FROM \"user\" WHERE id = ?";
        return sqlExecutor.executeUpdate(sql, new Object[]{id});
    }
    
    /**
     * Get all users for a specific campus.
     * 
     * @param campusId The campus ID
     * @return List of users at that campus
     */
    public List<User> findByCampusId(Long campusId) {
        String sql = "SELECT id, first_name, last_name, email, password, campus_id, COALESCE(is_admin, FALSE) as is_admin " +
                     "FROM \"user\" WHERE campus_id = ?";
        
        List<Map<String, Object>> results = sqlExecutor.executeQuery(sql, new Object[]{campusId});
        
        return results.stream()
                .map(this::mapRowToUser)
                .toList();
    }
    
    /**
     * Map a database row to a User object.
     */
    private User mapRowToUser(Map<String, Object> row) {
        User user = new User();
        user.setId(((Number) row.get("id")).longValue());
        user.setFirstName((String) row.get("first_name"));
        user.setLastName((String) row.get("last_name"));
        user.setEmail((String) row.get("email"));
        user.setPassword((String) row.get("password"));
        user.setCampusId(((Number) row.get("campus_id")).longValue());
        
        // Handle is_admin field (may be null for existing records)
        Object isAdminObj = row.get("is_admin");
        if (isAdminObj instanceof Boolean) {
            user.setIsAdmin((Boolean) isAdminObj);
        } else if (isAdminObj != null) {
            user.setIsAdmin(((Boolean) isAdminObj));
        } else {
            user.setIsAdmin(false);
        }
        
        return user;
    }
}
