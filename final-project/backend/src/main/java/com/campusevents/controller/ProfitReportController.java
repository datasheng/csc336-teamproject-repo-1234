package com.campusevents.controller;

import com.campusevents.dto.ErrorResponse;
import com.campusevents.dto.ProfitReportDTO;
import com.campusevents.model.User;
import com.campusevents.security.CurrentUser;
import com.campusevents.service.ProfitReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for profit reporting endpoints (admin only).
 * 
 * Endpoints:
 * - GET /api/admin/profit - Get comprehensive profit report
 */
@RestController
@RequestMapping("/api/admin/profit")
public class ProfitReportController {
    
    private final ProfitReportService profitReportService;
    
    public ProfitReportController(ProfitReportService profitReportService) {
        this.profitReportService = profitReportService;
    }
    
    /**
     * Get comprehensive profit report.
     * Only accessible to admin users.
     * 
     * @param user The authenticated user (must be admin)
     * @return Profit report data
     */
    @GetMapping
    public ResponseEntity<?> getProfitReport(@CurrentUser User user) {
        // Check if user is admin
        if (user == null || !Boolean.TRUE.equals(user.getIsAdmin())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponse("Forbidden", "Admin access required", 403)
            );
        }
        
        try {
            ProfitReportDTO report = profitReportService.getProfitReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "Failed to generate profit report: " + e.getMessage(), 500)
            );
        }
    }
}

