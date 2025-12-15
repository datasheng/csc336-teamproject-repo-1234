package com.campusevents.service;

import com.campusevents.dto.ProfitReportDTO;
import com.campusevents.util.SqlExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for calculating profit reports.
 * Profit is calculated as 10% of all ticket sales revenue.
 */
@Service
public class ProfitReportService {
    
    private static final BigDecimal PROFIT_PERCENTAGE = new BigDecimal("0.10"); // 10%
    
    private final SqlExecutor sqlExecutor;
    
    public ProfitReportService(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }
    
    /**
     * Get comprehensive profit report.
     * 
     * @return ProfitReportDTO with all profit data
     */
    public ProfitReportDTO getProfitReport() {
        ProfitReportDTO report = new ProfitReportDTO();
        
        // Calculate total revenue and profit
        String totalRevenueSql = 
            "SELECT COALESCE(SUM(c.cost), 0) as total_revenue, COUNT(*) as total_tickets " +
            "FROM ticket t " +
            "JOIN cost c ON t.event_id = c.event_id AND t.type = c.type";
        
        Map<String, Object> totalResult = sqlExecutor.executeQueryForMap(totalRevenueSql, new Object[]{});
        BigDecimal totalRevenue = totalResult != null && totalResult.get("total_revenue") != null 
            ? (BigDecimal) totalResult.get("total_revenue") 
            : BigDecimal.ZERO;
        Long totalTickets = totalResult != null && totalResult.get("total_tickets") != null
            ? ((Number) totalResult.get("total_tickets")).longValue()
            : 0L;
        
        BigDecimal totalProfit = totalRevenue.multiply(PROFIT_PERCENTAGE)
            .setScale(2, RoundingMode.HALF_UP);
        
        report.setTotalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP));
        report.setTotalProfit(totalProfit);
        report.setTotalTicketsSold(totalTickets);
        
        // Get profit by event
        report.setProfitByEvent(getProfitByEvent());
        
        // Get profit by date
        report.setProfitByDate(getProfitByDate());
        
        report.setLastUpdated(LocalDateTime.now());
        
        return report;
    }
    
    /**
     * Get profit breakdown by event.
     */
    private List<ProfitReportDTO.ProfitByEventDTO> getProfitByEvent() {
        String sql = 
            "SELECT e.id as event_id, e.description, o.name as organizer_name, " +
            "COUNT(t.user_id) as tickets_sold, " +
            "COALESCE(SUM(c.cost), 0) as revenue " +
            "FROM event e " +
            "LEFT JOIN ticket t ON e.id = t.event_id " +
            "LEFT JOIN cost c ON t.event_id = c.event_id AND t.type = c.type " +
            "LEFT JOIN organization o ON e.organizer_id = o.id " +
            "GROUP BY e.id, e.description, o.name " +
            "HAVING COUNT(t.user_id) > 0 " +
            "ORDER BY revenue DESC";
        
        List<Map<String, Object>> results = sqlExecutor.executeQuery(sql, new Object[]{});
        
        return results.stream()
            .map(row -> {
                ProfitReportDTO.ProfitByEventDTO dto = new ProfitReportDTO.ProfitByEventDTO();
                dto.setEventId(((Number) row.get("event_id")).longValue());
                dto.setEventDescription((String) row.get("description"));
                dto.setOrganizerName((String) row.get("organizer_name"));
                dto.setTicketsSold(((Number) row.get("tickets_sold")).longValue());
                
                BigDecimal revenue = row.get("revenue") != null 
                    ? (BigDecimal) row.get("revenue")
                    : BigDecimal.ZERO;
                dto.setRevenue(revenue.setScale(2, RoundingMode.HALF_UP));
                
                BigDecimal profit = revenue.multiply(PROFIT_PERCENTAGE)
                    .setScale(2, RoundingMode.HALF_UP);
                dto.setProfit(profit);
                
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get profit breakdown by date.
     */
    private List<ProfitReportDTO.ProfitByDateDTO> getProfitByDate() {
        // Get tickets grouped by purchase date (using time_period or a created_at if available)
        // Since we don't have a purchase timestamp, we'll group by the event start_time date
        // or use the fee period start_time as a proxy
        String sql = 
            "SELECT DATE(f.start_time) as purchase_date, " +
            "COUNT(t.user_id) as tickets_sold, " +
            "COALESCE(SUM(c.cost), 0) as revenue " +
            "FROM ticket t " +
            "JOIN cost c ON t.event_id = c.event_id AND t.type = c.type " +
            "JOIN fee f ON t.time_period = f.id " +
            "GROUP BY DATE(f.start_time) " +
            "ORDER BY purchase_date DESC " +
            "LIMIT 30"; // Last 30 days
        
        List<Map<String, Object>> results = sqlExecutor.executeQuery(sql, new Object[]{});
        
        return results.stream()
            .map(row -> {
                ProfitReportDTO.ProfitByDateDTO dto = new ProfitReportDTO.ProfitByDateDTO();
                
                Object dateObj = row.get("purchase_date");
                if (dateObj instanceof Timestamp) {
                    dto.setDate(((Timestamp) dateObj).toLocalDateTime().toLocalDate().toString());
                } else if (dateObj instanceof LocalDate) {
                    dto.setDate(((LocalDate) dateObj).toString());
                } else {
                    dto.setDate(dateObj.toString());
                }
                
                dto.setTicketsSold(((Number) row.get("tickets_sold")).longValue());
                
                BigDecimal revenue = row.get("revenue") != null 
                    ? (BigDecimal) row.get("revenue")
                    : BigDecimal.ZERO;
                dto.setRevenue(revenue.setScale(2, RoundingMode.HALF_UP));
                
                BigDecimal profit = revenue.multiply(PROFIT_PERCENTAGE)
                    .setScale(2, RoundingMode.HALF_UP);
                dto.setProfit(profit);
                
                return dto;
            })
            .collect(Collectors.toList());
    }
}

