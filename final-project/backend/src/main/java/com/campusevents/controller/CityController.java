package com.campusevents.controller;

import com.campusevents.dto.CityDTO;
import com.campusevents.dto.ErrorResponse;
import com.campusevents.util.SqlExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST controller for city endpoints.
 * 
 * Endpoints:
 * - GET /api/cities - Get all cities
 */
@RestController
@RequestMapping("/api/cities")
public class CityController {
    
    private final SqlExecutor sqlExecutor;
    
    public CityController(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }
    
    /**
     * Get all cities.
     * 
     * @return List of cities with their countries
     */
    @GetMapping
    public ResponseEntity<?> getAllCities() {
        try {
            String sql = "SELECT city, country FROM city ORDER BY city";
            List<Map<String, Object>> results = sqlExecutor.executeQuery(sql, null);
            
            List<CityDTO> cities = new ArrayList<>();
            for (Map<String, Object> row : results) {
                CityDTO city = new CityDTO(
                    (String) row.get("city"),
                    (String) row.get("country")
                );
                cities.add(city);
            }
            
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while fetching cities", 500)
            );
        }
    }
}
