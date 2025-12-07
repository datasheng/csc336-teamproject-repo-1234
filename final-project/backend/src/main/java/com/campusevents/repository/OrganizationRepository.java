package com.campusevents.repository;

import com.campusevents.model.Organization;
import com.campusevents.util.SqlExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for Organization database operations using raw SQL.
 * 
 * IMPORTANT: NO ORM is used. All queries are raw SQL with prepared statements.
 */
@Repository
public class OrganizationRepository {
    
    private final SqlExecutor sqlExecutor;
    
    public OrganizationRepository(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }
    
    /**
     * Find an organization by ID.
     * 
     * @param id The organization ID
     * @return Optional containing the organization if found, empty otherwise
     */
    public Optional<Organization> findById(Long id) {
        String sql = "SELECT id, name, description FROM organization WHERE id = ?";
        
        List<Map<String, Object>> results = sqlExecutor.executeQuery(sql, new Object[]{id});
        
        if (results.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(mapRowToOrganization(results.get(0)));
    }
    
    /**
     * Save a new organization to the database.
     * 
     * @param organization The organization to save
     * @return The saved organization with generated ID
     */
    public Organization save(Organization organization) {
        String sql = "INSERT INTO organization (name, description) VALUES (?, ?)";
        
        Long generatedId = sqlExecutor.executeInsert(sql, new Object[]{
            organization.getName(),
            organization.getDescription()
        });
        
        organization.setId(generatedId);
        return organization;
    }
    
    /**
     * Update an existing organization.
     * 
     * @param organization The organization with updated fields
     * @return Number of rows affected (should be 1)
     */
    public int update(Organization organization) {
        String sql = "UPDATE organization SET name = ?, description = ? WHERE id = ?";
        
        return sqlExecutor.executeUpdate(sql, new Object[]{
            organization.getName(),
            organization.getDescription(),
            organization.getId()
        });
    }
    
    /**
     * Add a user as a leader of an organization.
     * 
     * @param userId The user ID
     * @param orgId The organization ID
     */
    public void addLeader(Long userId, Long orgId) {
        String sql = "INSERT INTO org_leadership (user_id, org_id) VALUES (?, ?)";
        sqlExecutor.executeUpdate(sql, new Object[]{userId, orgId});
    }
    
    /**
     * Check if a user is a leader of an organization.
     * 
     * @param userId The user ID
     * @param orgId The organization ID
     * @return true if the user is a leader of the organization
     */
    public boolean isLeader(Long userId, Long orgId) {
        String sql = "SELECT 1 FROM org_leadership WHERE user_id = ? AND org_id = ?";
        return sqlExecutor.exists(sql, new Object[]{userId, orgId});
    }
    
    /**
     * Map a database row to an Organization object.
     */
    private Organization mapRowToOrganization(Map<String, Object> row) {
        Organization organization = new Organization();
        organization.setId(((Number) row.get("id")).longValue());
        organization.setName((String) row.get("name"));
        organization.setDescription((String) row.get("description"));
        return organization;
    }
}
