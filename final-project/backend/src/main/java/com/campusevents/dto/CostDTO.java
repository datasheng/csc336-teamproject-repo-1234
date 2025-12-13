package com.campusevents.dto;

import java.math.BigDecimal;

/**
 * DTO for cost/ticket type information.
 */
public class CostDTO {
    
    private String type;
    private BigDecimal cost;
    
    // Default constructor
    public CostDTO() {}
    
    public CostDTO(String type, BigDecimal cost) {
        this.type = type;
        this.cost = cost;
    }
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public BigDecimal getCost() {
        return cost;
    }
    
    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
}
