package com.campusevents.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for profit reporting data.
 */
public class ProfitReportDTO {
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit; // 10% of revenue
    private Long totalTicketsSold;
    private List<ProfitByEventDTO> profitByEvent;
    private List<ProfitByDateDTO> profitByDate;
    private LocalDateTime lastUpdated;
    
    public ProfitReportDTO() {}
    
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public BigDecimal getTotalProfit() {
        return totalProfit;
    }
    
    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }
    
    public Long getTotalTicketsSold() {
        return totalTicketsSold;
    }
    
    public void setTotalTicketsSold(Long totalTicketsSold) {
        this.totalTicketsSold = totalTicketsSold;
    }
    
    public List<ProfitByEventDTO> getProfitByEvent() {
        return profitByEvent;
    }
    
    public void setProfitByEvent(List<ProfitByEventDTO> profitByEvent) {
        this.profitByEvent = profitByEvent;
    }
    
    public List<ProfitByDateDTO> getProfitByDate() {
        return profitByDate;
    }
    
    public void setProfitByDate(List<ProfitByDateDTO> profitByDate) {
        this.profitByDate = profitByDate;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public static class ProfitByEventDTO {
        private Long eventId;
        private String eventDescription;
        private String organizerName;
        private Long ticketsSold;
        private BigDecimal revenue;
        private BigDecimal profit;
        
        public Long getEventId() {
            return eventId;
        }
        
        public void setEventId(Long eventId) {
            this.eventId = eventId;
        }
        
        public String getEventDescription() {
            return eventDescription;
        }
        
        public void setEventDescription(String eventDescription) {
            this.eventDescription = eventDescription;
        }
        
        public String getOrganizerName() {
            return organizerName;
        }
        
        public void setOrganizerName(String organizerName) {
            this.organizerName = organizerName;
        }
        
        public Long getTicketsSold() {
            return ticketsSold;
        }
        
        public void setTicketsSold(Long ticketsSold) {
            this.ticketsSold = ticketsSold;
        }
        
        public BigDecimal getRevenue() {
            return revenue;
        }
        
        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }
        
        public BigDecimal getProfit() {
            return profit;
        }
        
        public void setProfit(BigDecimal profit) {
            this.profit = profit;
        }
    }
    
    public static class ProfitByDateDTO {
        private String date;
        private Long ticketsSold;
        private BigDecimal revenue;
        private BigDecimal profit;
        
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
        
        public Long getTicketsSold() {
            return ticketsSold;
        }
        
        public void setTicketsSold(Long ticketsSold) {
            this.ticketsSold = ticketsSold;
        }
        
        public BigDecimal getRevenue() {
            return revenue;
        }
        
        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }
        
        public BigDecimal getProfit() {
            return profit;
        }
        
        public void setProfit(BigDecimal profit) {
            this.profit = profit;
        }
    }
}

