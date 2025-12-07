package com.campusevents.controller;

import com.campusevents.dto.CampusDTO;
import com.campusevents.dto.CampusDetailDTO;
import com.campusevents.dto.ErrorResponse;
import com.campusevents.util.SqlExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST controller for campus endpoints.
 * 
 * Endpoints:
 * - GET /api/campuses - Get campuses by city
 * - GET /api/campuses/{id} - Get campus details by ID
 */
@RestController
@RequestMapping("/api/campuses")
public class CampusController {
    
    private final SqlExecutor sqlExecutor;
    
    public CampusController(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }
    
    /**
     * Get campuses filtered by city.
     * 
     * @param city The city to filter by
     * @return List of campuses in the specified city
     */
    @GetMapping
    public ResponseEntity<?> getCampusesByCity(@RequestParam String city) {
        try {
            String sql = "SELECT id, name, address, zip_code, city FROM campus WHERE city = ? ORDER BY name";
            List<Map<String, Object>> results = sqlExecutor.executeQuery(sql, new Object[]{city});
            
            List<CampusDTO> campuses = new ArrayList<>();
            for (Map<String, Object> row : results) {
                CampusDTO campus = new CampusDTO(
                    ((Number) row.get("id")).longValue(),
                    (String) row.get("name"),
                    (String) row.get("address"),
                    (String) row.get("zip_code"),
                    (String) row.get("city")
                );
                campuses.add(campus);
            }
            
            return ResponseEntity.ok(campuses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while fetching campuses", 500)
            );
        }
    }
    
    /**
     * Get campus details by ID.
     * 
     * @param id The campus ID
     * @return Campus details including country from joined city table
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCampusById(@PathVariable Long id) {
        try {
            String sql = "SELECT c.id, c.name, c.address, c.zip_code, c.city, ci.country " +
                         "FROM campus c JOIN city ci ON c.city = ci.city WHERE c.id = ?";
            List<Map<String, Object>> results = sqlExecutor.executeQuery(sql, new Object[]{id});
            
            if (results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "Campus not found", 404)
                );
            }
            
            Map<String, Object> row = results.get(0);
            CampusDetailDTO campus = new CampusDetailDTO(
                ((Number) row.get("id")).longValue(),
                (String) row.get("name"),
                (String) row.get("address"),
                (String) row.get("zip_code"),
                (String) row.get("city"),
                (String) row.get("country")
            );
            
            return ResponseEntity.ok(campus);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while fetching campus details", 500)
            );
        }
    }
}
