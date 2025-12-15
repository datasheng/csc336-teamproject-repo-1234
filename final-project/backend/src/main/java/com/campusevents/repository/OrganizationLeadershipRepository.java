package com.campusevents.repository;

import com.campusevents.dto.LeaderDTO;
import com.campusevents.util.SqlExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository for Organization Leadership database operations using raw SQL.
 * 
 * IMPORTANT: NO ORM is used. All queries are raw SQL with prepared statements.
 */
@Repository
public class OrganizationLeadershipRepository {
    
    private final SqlExecutor sqlExecutor;
    
    public OrganizationLeadershipRepository(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }
    
    /**
     * Get all leaders for an organization.
     * 
     * @param orgId The organization ID
     * @return List of leaders for the organization
     */
    public List<LeaderDTO> findLeadersByOrgId(Long orgId) {
        String sql = "SELECT u.id, u.first_name, u.last_name, u.email " +
                     "FROM org_leadership ol " +
                     "JOIN \"user\" u ON ol.user_id = u.id " +
                     "WHERE ol.org_id = ? " +
                     "ORDER BY u.last_name";
        
        List<Map<String, Object>> results = sqlExecutor.executeQuery(sql, new Object[]{orgId});
        
        return results.stream()
                .map(this::mapRowToLeaderDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Find a user ID by email address.
     * 
     * @param email The email to search for
     * @return Optional containing the user ID if found
     */
    public Optional<Long> findUserIdByEmail(String email) {
        String sql = "SELECT id FROM \"user\" WHERE email = ?";
        
        List<Map<String, Object>> results = sqlExecutor.executeQuery(sql, new Object[]{email});
        
        if (results.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(((Number) results.get(0).get("id")).longValue());
    }
    
    /**
     * Check if a user is already a leader of an organization.
     * 
     * @param userId The user ID
     * @param orgId The organization ID
     * @return true if the user is already a leader
     */
    public boolean isLeader(Long userId, Long orgId) {
        String sql = "SELECT 1 FROM org_leadership WHERE user_id = ? AND org_id = ?";
        return sqlExecutor.exists(sql, new Object[]{userId, orgId});
    }
    
    /**
     * Add a user as a leader of an organization.
     * Uses SQL function add_org_leader for validation and insertion.
     * 
     * @param userId The user ID
     * @param orgId The organization ID
     */
    public void addLeader(Long userId, Long orgId) {
        // Use SQL function for adding leader
        try {
            String sql = "SELECT add_org_leader(?, ?)";
            String result = sqlExecutor.executeScalar(sql, new Object[]{userId.intValue(), orgId.intValue()}, String.class);
            
            if (result != null && !result.equals("SUCCESS")) {
                // Log or handle specific errors if needed
                // For now, fall through to ensure leader is added
                if (!result.equals("ALREADY_LEADER")) {
                    throw new RuntimeException("Failed to add leader: " + result);
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            // Fallback to direct insert
            String sql = "INSERT INTO org_leadership (user_id, org_id) VALUES (?, ?)";
            sqlExecutor.executeUpdate(sql, new Object[]{userId, orgId});
        }
    }
    
    /**
     * Remove a user as a leader of an organization.
     * 
     * @param userId The user ID
     * @param orgId The organization ID
     * @return Number of rows affected
     */
    public int removeLeader(Long userId, Long orgId) {
        String sql = "DELETE FROM org_leadership WHERE user_id = ? AND org_id = ?";
        return sqlExecutor.executeUpdate(sql, new Object[]{userId, orgId});
    }
    
    /**
     * Count the number of leaders for an organization.
     * 
     * @param orgId The organization ID
     * @return The number of leaders
     */
    public int countLeaders(Long orgId) {
        String sql = "SELECT COUNT(*) FROM org_leadership WHERE org_id = ?";
        Long count = sqlExecutor.executeScalar(sql, new Object[]{orgId}, Long.class);
        return count != null ? count.intValue() : 0;
    }
    
    /**
     * Check if an organization exists.
     * 
     * @param orgId The organization ID
     * @return true if the organization exists
     */
    public boolean organizationExists(Long orgId) {
        String sql = "SELECT 1 FROM organization WHERE id = ?";
        return sqlExecutor.exists(sql, new Object[]{orgId});
    }
    
    /**
     * Map a database row to a LeaderDTO.
     */
    private LeaderDTO mapRowToLeaderDTO(Map<String, Object> row) {
        return new LeaderDTO(
            ((Number) row.get("id")).longValue(),
            (String) row.get("first_name"),
            (String) row.get("last_name"),
            (String) row.get("email")
        );
    }
}
