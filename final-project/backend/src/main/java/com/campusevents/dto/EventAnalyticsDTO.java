package com.campusevents.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for event analytics response.
 * Contains ticket sales breakdown by type and total metrics.
 */
public class EventAnalyticsDTO {
    
    private Long eventId;
    private Long totalTickets;
    private BigDecimal totalRevenue;
    private List<TicketTypeAnalyticsDTO> byType;
    
    public EventAnalyticsDTO() {}
    
    public EventAnalyticsDTO(Long eventId, Long totalTickets, BigDecimal totalRevenue, 
                              List<TicketTypeAnalyticsDTO> byType) {
        this.eventId = eventId;
        this.totalTickets = totalTickets;
        this.totalRevenue = totalRevenue;
        this.byType = byType;
    }
    
    public Long getEventId() {
        return eventId;
    }
    
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    
    public Long getTotalTickets() {
        return totalTickets;
    }
    
    public void setTotalTickets(Long totalTickets) {
        this.totalTickets = totalTickets;
    }
    
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public List<TicketTypeAnalyticsDTO> getByType() {
        return byType;
    }
    
    public void setByType(List<TicketTypeAnalyticsDTO> byType) {
        this.byType = byType;
    }
}
