package com.campusevents.service;

import com.campusevents.dto.PurchaseTicketRequest;
import com.campusevents.dto.TicketConfirmationDTO;
import com.campusevents.dto.UserTicketDTO;
import com.campusevents.util.SqlExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for ticket management operations.
 * 
 * Handles:
 * - Ticket purchasing with capacity validation
 * - Retrieving user's tickets
 * 
 * IMPORTANT: NO ORM is used. All queries are raw SQL with prepared statements.
 */
@Service
public class TicketService {
    
    private final SqlExecutor sqlExecutor;
    private final PubSubService pubSubService;
    
    public TicketService(SqlExecutor sqlExecutor, PubSubService pubSubService) {
        this.sqlExecutor = sqlExecutor;
        this.pubSubService = pubSubService;
    }
    
    /**
     * Check if an event exists.
     * 
     * @param eventId The event ID
     * @return true if the event exists
     */
    public boolean eventExists(Long eventId) {
        String sql = "SELECT 1 FROM event WHERE id = ?";
        return sqlExecutor.exists(sql, new Object[]{eventId});
    }
    
    /**
     * Get the capacity of an event.
     * 
     * @param eventId The event ID
     * @return The event capacity, or null if not found
     */
    public Integer getEventCapacity(Long eventId) {
        String sql = "SELECT capacity FROM event WHERE id = ?";
        Map<String, Object> result = sqlExecutor.executeQueryForMap(sql, new Object[]{eventId});
        if (result == null) {
            return null;
        }
        return ((Number) result.get("capacity")).intValue();
    }
    
    /**
     * Get the number of tickets sold for an event.
     * 
     * @param eventId The event ID
     * @return The number of tickets sold
     */
    public Long getTicketsSold(Long eventId) {
        String sql = "SELECT COUNT(*) as count FROM ticket WHERE event_id = ?";
        Map<String, Object> result = sqlExecutor.executeQueryForMap(sql, new Object[]{eventId});
        if (result == null) {
            return 0L;
        }
        return ((Number) result.get("count")).longValue();
    }
    
    /**
     * Get the cost for a specific ticket type at an event.
     * 
     * @param eventId The event ID
     * @param type The ticket type
     * @return The cost, or null if not found
     */
    public BigDecimal getTicketCost(Long eventId, String type) {
        String sql = "SELECT cost FROM cost WHERE event_id = ? AND type = ?";
        Map<String, Object> result = sqlExecutor.executeQueryForMap(sql, new Object[]{eventId, type});
        if (result == null) {
            return null;
        }
        return (BigDecimal) result.get("cost");
    }
    
    /**
     * Get event description for confirmation.
     * 
     * @param eventId The event ID
     * @return The event description
     */
    public String getEventDescription(Long eventId) {
        String sql = "SELECT description FROM event WHERE id = ?";
        Map<String, Object> result = sqlExecutor.executeQueryForMap(sql, new Object[]{eventId});
        if (result == null) {
            return null;
        }
        return (String) result.get("description");
    }
    
    /**
     * Get the campus ID for an event.
     * 
     * @param eventId The event ID
     * @return The campus ID, or null if not found
     */
    public Long getEventCampusId(Long eventId) {
        String sql = "SELECT campus_id FROM event WHERE id = ?";
        Map<String, Object> result = sqlExecutor.executeQueryForMap(sql, new Object[]{eventId});
        if (result == null) {
            return null;
        }
        return ((Number) result.get("campus_id")).longValue();
    }
    
    /**
     * Check if user already has a ticket of this type for this event.
     * 
     * @param userId The user ID
     * @param eventId The event ID
     * @param type The ticket type
     * @return true if the user already has this ticket
     */
    public boolean userHasTicket(Long userId, Long eventId, String type) {
        String sql = "SELECT 1 FROM ticket WHERE user_id = ? AND event_id = ? AND type = ?";
        return sqlExecutor.exists(sql, new Object[]{userId, eventId, type});
    }
    
    /**
     * Get the current fee period ID.
     * Returns the fee period that contains NOW().
     * If no fee period exists, returns null.
     * 
     * @return The current fee period ID, or null if none found
     */
    public Integer getCurrentFeePeriodId() {
        String sql = "SELECT id FROM fee WHERE start_time <= NOW() AND end_time >= NOW() LIMIT 1";
        Map<String, Object> result = sqlExecutor.executeQueryForMap(sql, new Object[]{});
        if (result == null) {
            // If no fee period exists, try to get the first one or default to 1
            String fallbackSql = "SELECT id FROM fee ORDER BY id LIMIT 1";
            Map<String, Object> fallbackResult = sqlExecutor.executeQueryForMap(fallbackSql, new Object[]{});
            if (fallbackResult == null) {
                return 1; // Default to 1 if no fee periods exist
            }
            return ((Number) fallbackResult.get("id")).intValue();
        }
        return ((Number) result.get("id")).intValue();
    }
    
    /**
     * Purchase a ticket for an event.
     * 
     * @param userId The user ID
     * @param request The purchase request
     * @return The ticket confirmation
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if capacity exceeded or already purchased
     */
    @Transactional
    public TicketConfirmationDTO purchaseTicket(Long userId, PurchaseTicketRequest request) {
        Long eventId = request.getEventId();
        String type = request.getType();
        
        // Check event exists
        if (!eventExists(eventId)) {
            throw new IllegalArgumentException("Event not found");
        }
        
        // Check capacity
        Integer capacity = getEventCapacity(eventId);
        Long ticketsSold = getTicketsSold(eventId);
        
        if (ticketsSold >= capacity) {
            throw new IllegalStateException("Event is sold out");
        }
        
        // Check cost type exists
        BigDecimal cost = getTicketCost(eventId, type);
        if (cost == null) {
            throw new IllegalArgumentException("Invalid ticket type for this event");
        }
        
        // Check if user already has this ticket
        if (userHasTicket(userId, eventId, type)) {
            throw new IllegalStateException("You already have a ticket of this type for this event");
        }
        
        // Simulate payment processing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Payment processing interrupted");
        }
        
        // Get current fee period
        Integer feePeriodId = getCurrentFeePeriodId();
        
        String insertSql = "INSERT INTO ticket (user_id, event_id, type, time_period) VALUES (?, ?, ?, ?)";
        sqlExecutor.executeUpdate(insertSql, new Object[]{userId, eventId, type, feePeriodId});

        // Calculate new ticket counts for real-time updates
        Long newTicketsSold = ticketsSold + 1;
        Integer remainingCapacity = capacity - newTicketsSold.intValue();
        
        // Get campusId and organizerId for dashboard updates
        Long campusId = getEventCampusId(eventId);
        Long organizerId = getEventOrganizerId(eventId);

        try {
            pubSubService.publishTicketPurchased(eventId, userId, type, newTicketsSold, remainingCapacity, campusId);
            // Also publish analytics update for org dashboard
            if (organizerId != null) {
                pubSubService.publishAnalyticsUpdated(eventId, organizerId);
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to publish Pub/Sub message: " + e.getMessage());
        }

        TicketConfirmationDTO confirmation = new TicketConfirmationDTO();
        confirmation.setEventId(eventId);
        confirmation.setUserId(userId);
        confirmation.setType(type);
        confirmation.setCost(cost);
        confirmation.setEventDescription(getEventDescription(eventId));
        confirmation.setMessage("Ticket purchased successfully");
        
        return confirmation;
    }
    
    /**
     * Get the organizer ID for an event.
     * 
     * @param eventId The event ID
     * @return The organizer ID, or null if not found
     */
    public Long getEventOrganizerId(Long eventId) {
        String sql = "SELECT organizer_id FROM event WHERE id = ?";
        Map<String, Object> result = sqlExecutor.executeQueryForMap(sql, new Object[]{eventId});
        if (result == null) {
            return null;
        }
        return ((Number) result.get("organizer_id")).longValue();
    }
    
    /**
     * Get all tickets for a user with event details.
     * 
     * @param userId The user ID
     * @return List of user's tickets with event details
     */
    public List<UserTicketDTO> getUserTickets(Long userId) {
        String sql = "SELECT t.event_id, t.type, e.description, e.start_time, e.end_time, " +
                     "o.name as organizer_name, c.cost " +
                     "FROM ticket t " +
                     "JOIN event e ON t.event_id = e.id " +
                     "JOIN organization o ON e.organizer_id = o.id " +
                     "JOIN cost c ON t.event_id = c.event_id AND t.type = c.type " +
                     "WHERE t.user_id = ? " +
                     "ORDER BY e.start_time";
        
        List<Map<String, Object>> rows = sqlExecutor.executeQuery(sql, new Object[]{userId});
        
        return rows.stream()
            .map(this::mapRowToUserTicketDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Map a database row to a UserTicketDTO.
     */
    private UserTicketDTO mapRowToUserTicketDTO(Map<String, Object> row) {
        UserTicketDTO dto = new UserTicketDTO();
        dto.setEventId(((Number) row.get("event_id")).longValue());
        dto.setType((String) row.get("type"));
        dto.setEventDescription((String) row.get("description"));
        dto.setOrganizerName((String) row.get("organizer_name"));
        dto.setCost((BigDecimal) row.get("cost"));
        
        // Handle timestamp conversion
        Object startTimeObj = row.get("start_time");
        Object endTimeObj = row.get("end_time");
        
        if (startTimeObj instanceof Timestamp) {
            dto.setStartTime(((Timestamp) startTimeObj).toLocalDateTime());
        } else if (startTimeObj instanceof LocalDateTime) {
            dto.setStartTime((LocalDateTime) startTimeObj);
        }
        
        if (endTimeObj instanceof Timestamp) {
            dto.setEndTime(((Timestamp) endTimeObj).toLocalDateTime());
        } else if (endTimeObj instanceof LocalDateTime) {
            dto.setEndTime((LocalDateTime) endTimeObj);
        }
        
        return dto;
    }
}
