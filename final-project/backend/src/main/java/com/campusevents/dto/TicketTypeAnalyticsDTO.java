package com.campusevents.dto;

import java.math.BigDecimal;

/**
 * DTO for ticket analytics by type.
 */
public class TicketTypeAnalyticsDTO {
    
    private String type;
    private Long count;
    private BigDecimal revenue;
    
    public TicketTypeAnalyticsDTO() {}
    
    public TicketTypeAnalyticsDTO(String type, Long count, BigDecimal revenue) {
        this.type = type;
        this.count = count;
        this.revenue = revenue;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Long getCount() {
        return count;
    }
    
    public void setCount(Long count) {
        this.count = count;
    }
    
    public BigDecimal getRevenue() {
        return revenue;
    }
    
    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}
