package com.campusevents.service;

import com.campusevents.dto.CostDTO;
import com.campusevents.dto.CreateEventRequest;
import com.campusevents.dto.EventAnalyticsDTO;
import com.campusevents.dto.EventDTO;
import com.campusevents.dto.TicketTypeAnalyticsDTO;
import com.campusevents.dto.UpdateEventRequest;
import com.campusevents.util.SqlExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for event management operations.
 * 
 * Handles:
 * - Event creation with costs
 * - Event retrieval with details and availability
 * - Event updates
 * 
 * IMPORTANT: NO ORM is used. All queries are raw SQL with prepared statements.
 */
@Service
public class EventService {
    
    private final SqlExecutor sqlExecutor;
    private final PubSubService pubSubService;
    
    public EventService(SqlExecutor sqlExecutor, PubSubService pubSubService) {
        this.sqlExecutor = sqlExecutor;
        this.pubSubService = pubSubService;
    }
    
    /**
     * Check if a user is a leader of an organization.
     * 
     * @param userId The user ID
     * @param orgId The organization ID
     * @return true if the user is a leader
     */
    public boolean isLeader(Long userId, Long orgId) {
        String sql = "SELECT 1 FROM org_leadership WHERE user_id = ? AND org_id = ?";
        return sqlExecutor.exists(sql, new Object[]{userId, orgId});
    }
    
    /**
     * Check if an organization exists.
     * 
     * @param orgId The organization ID
     * @return true if the organization exists
     */
    public boolean organizationExists(Long orgId) {
        String sql = "SELECT 1 FROM organization WHERE id = ?";
        return sqlExecutor.exists(sql, new Object[]{orgId});
    }
    
    /**
     * Check if a campus exists.
     * 
     * @param campusId The campus ID
     * @return true if the campus exists
     */
    public boolean campusExists(Long campusId) {
        String sql = "SELECT 1 FROM campus WHERE id = ?";
        return sqlExecutor.exists(sql, new Object[]{campusId});
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
     * Get the organizer ID for an event.
     * 
     * @param eventId The event ID
     * @return Optional containing the organizer ID if found
     */
    public Optional<Long> getEventOrganizerId(Long eventId) {
        String sql = "SELECT organizer_id FROM event WHERE id = ?";
        Map<String, Object> result = sqlExecutor.executeQueryForMap(sql, new Object[]{eventId});
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(((Number) result.get("organizer_id")).longValue());
    }
    
    /**
     * Create a new event with associated costs.
     * 
     * @param request The event creation request
     * @return The created event DTO with ID
     */
    @Transactional
    public EventDTO createEvent(CreateEventRequest request) {
        // Insert the event
        String eventSql = "INSERT INTO event (organizer_id, campus_id, capacity, description, start_time, end_time) " +
                          "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        
        // We use executeInsert which returns the generated ID
        Long eventId = sqlExecutor.executeInsert(
            "INSERT INTO event (organizer_id, campus_id, capacity, description, start_time, end_time) " +
            "VALUES (?, ?, ?, ?, ?, ?)",
            new Object[]{
                request.getOrganizerId(),
                request.getCampusId(),
                request.getCapacity(),
                request.getDescription(),
                Timestamp.valueOf(request.getStartTime()),
                Timestamp.valueOf(request.getEndTime())
            }
        );
        
        // Insert costs if provided
        if (request.getCosts() != null && !request.getCosts().isEmpty()) {
            for (CostDTO cost : request.getCosts()) {
                String costSql = "INSERT INTO cost (type, event_id, cost) VALUES (?, ?, ?)";
                sqlExecutor.executeUpdate(costSql, new Object[]{
                    cost.getType(),
                    eventId,
                    cost.getCost()
                });
            }
        }
        
        // Insert tags if provided
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            saveEventTags(eventId, request.getTags());
        }
        
        // Publish Pub/Sub message
        pubSubService.publishEventCreated(eventId, request.getOrganizerId(), request.getCampusId());
        
        // Fetch and return the complete event
        return getEventById(eventId).orElseThrow(() -> 
            new RuntimeException("Failed to retrieve created event"));
    }
    
    /**
     * Get event by ID with full details.
     * 
     * @param eventId The event ID
     * @return Optional containing the event DTO if found
     */
    public Optional<EventDTO> getEventById(Long eventId) {
        // Fetch event with organizer and campus names
        String eventSql = "SELECT e.id, e.organizer_id, e.campus_id, e.capacity, e.description, " +
                          "e.start_time, e.end_time, o.name as organizer_name, c.name as campus_name " +
                          "FROM event e " +
                          "JOIN organization o ON e.organizer_id = o.id " +
                          "JOIN campus c ON e.campus_id = c.id " +
                          "WHERE e.id = ?";
        
        Map<String, Object> eventRow = sqlExecutor.executeQueryForMap(eventSql, new Object[]{eventId});
        
        if (eventRow == null) {
            return Optional.empty();
        }
        
        // Fetch costs
        String costsSql = "SELECT type, cost FROM cost WHERE event_id = ?";
        List<Map<String, Object>> costsRows = sqlExecutor.executeQuery(costsSql, new Object[]{eventId});
        
        List<CostDTO> costs = costsRows.stream()
            .map(row -> new CostDTO(
                (String) row.get("type"),
                (BigDecimal) row.get("cost")
            ))
            .collect(Collectors.toList());
        
        // Fetch tickets sold count
        String ticketsSql = "SELECT COUNT(*) as tickets_sold FROM ticket WHERE event_id = ?";
        Map<String, Object> ticketsRow = sqlExecutor.executeQueryForMap(ticketsSql, new Object[]{eventId});
        Long ticketsSold = ticketsRow != null ? ((Number) ticketsRow.get("tickets_sold")).longValue() : 0L;
        
        // Fetch tags
        List<String> tags = getEventTags(eventId);
        
        // Build the EventDTO
        EventDTO eventDTO = new EventDTO();
        eventDTO.setId(((Number) eventRow.get("id")).longValue());
        eventDTO.setOrganizerId(((Number) eventRow.get("organizer_id")).longValue());
        eventDTO.setOrganizerName((String) eventRow.get("organizer_name"));
        eventDTO.setCampusId(((Number) eventRow.get("campus_id")).longValue());
        eventDTO.setCampusName((String) eventRow.get("campus_name"));
        eventDTO.setCapacity(((Number) eventRow.get("capacity")).intValue());
        eventDTO.setDescription((String) eventRow.get("description"));
        
        // Handle timestamp conversion
        Object startTimeObj = eventRow.get("start_time");
        Object endTimeObj = eventRow.get("end_time");
        
        if (startTimeObj instanceof Timestamp) {
            eventDTO.setStartTime(((Timestamp) startTimeObj).toLocalDateTime());
        } else if (startTimeObj instanceof LocalDateTime) {
            eventDTO.setStartTime((LocalDateTime) startTimeObj);
        }
        
        if (endTimeObj instanceof Timestamp) {
            eventDTO.setEndTime(((Timestamp) endTimeObj).toLocalDateTime());
        } else if (endTimeObj instanceof LocalDateTime) {
            eventDTO.setEndTime((LocalDateTime) endTimeObj);
        }
        
        eventDTO.setCosts(costs);
        eventDTO.setTicketsSold(ticketsSold);
        eventDTO.setAvailableCapacity(eventDTO.getCapacity() - ticketsSold.intValue());
        eventDTO.setTags(tags);
        
        return Optional.of(eventDTO);
    }
    
    /**
     * Update an existing event.
     * 
     * @param eventId The event ID to update
     * @param request The update request
     * @return The updated event DTO
     */
    @Transactional
    public EventDTO updateEvent(Long eventId, UpdateEventRequest request) {
        // Get organizer ID for real-time updates
        Optional<Long> organizerIdOpt = getEventOrganizerId(eventId);
        Long organizerId = organizerIdOpt.orElse(null);
        
        String updateSql = "UPDATE event SET capacity = ?, description = ?, start_time = ?, end_time = ? " +
                           "WHERE id = ?";
        
        sqlExecutor.executeUpdate(updateSql, new Object[]{
            request.getCapacity(),
            request.getDescription(),
            Timestamp.valueOf(request.getStartTime()),
            Timestamp.valueOf(request.getEndTime()),
            eventId
        });
        
        // Update tags if provided
        if (request.getTags() != null) {
            // Delete existing tags
            String deleteTagsSql = "DELETE FROM event_tag WHERE event_id = ?";
            sqlExecutor.executeUpdate(deleteTagsSql, new Object[]{eventId});
            
            // Insert new tags
            if (!request.getTags().isEmpty()) {
                saveEventTags(eventId, request.getTags());
            }
        }
        
        // Publish Pub/Sub message with org info for org page updates
        if (organizerId != null) {
            pubSubService.publishEventUpdatedWithOrg(eventId, organizerId);
        } else {
            pubSubService.publishEventUpdated(eventId);
        }
        
        // Fetch and return the updated event
        return getEventById(eventId).orElseThrow(() -> 
            new RuntimeException("Failed to retrieve updated event"));
    }
    
    /**
     * Delete an event.
     * Also deletes associated tickets and costs.
     * 
     * @param eventId The event ID to delete
     * @return true if deleted successfully
     */
    @Transactional
    public boolean deleteEvent(Long eventId) {
        // Get event info before deletion for real-time updates
        Optional<Long> organizerIdOpt = getEventOrganizerId(eventId);
        Long campusId = getEventCampusId(eventId);
        
        if (organizerIdOpt.isEmpty()) {
            return false;
        }
        
        Long organizerId = organizerIdOpt.get();
        
        // Delete associated tickets first (foreign key constraint)
        String deleteTicketsSql = "DELETE FROM ticket WHERE event_id = ?";
        sqlExecutor.executeUpdate(deleteTicketsSql, new Object[]{eventId});
        
        // Delete associated costs
        String deleteCostsSql = "DELETE FROM cost WHERE event_id = ?";
        sqlExecutor.executeUpdate(deleteCostsSql, new Object[]{eventId});
        
        // Delete associated tags
        String deleteTagsSql = "DELETE FROM event_tag WHERE event_id = ?";
        sqlExecutor.executeUpdate(deleteTagsSql, new Object[]{eventId});
        
        // Delete the event
        String deleteEventSql = "DELETE FROM event WHERE id = ?";
        int rowsAffected = sqlExecutor.executeUpdate(deleteEventSql, new Object[]{eventId});
        
        if (rowsAffected > 0) {
            // Publish real-time update
            pubSubService.publishEventDeletedWithOrg(eventId, organizerId, campusId);
        }
        
        return rowsAffected > 0;
    }
    
    /**
     * Cancel an event (marks it as cancelled but doesn't delete).
     * 
     * @param eventId The event ID to cancel
     * @return The updated event DTO
     */
    @Transactional
    public EventDTO cancelEvent(Long eventId) {
        // Get event info for real-time updates
        Optional<Long> organizerIdOpt = getEventOrganizerId(eventId);
        Long campusId = getEventCampusId(eventId);
        
        if (organizerIdOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found");
        }
        
        Long organizerId = organizerIdOpt.get();
        
        // Update event description to indicate cancelled
        String updateSql = "UPDATE event SET description = CONCAT('[CANCELLED] ', description) WHERE id = ? AND description NOT LIKE '[CANCELLED]%'";
        sqlExecutor.executeUpdate(updateSql, new Object[]{eventId});
        
        // Publish real-time update
        pubSubService.publishEventCancelledWithOrg(eventId, organizerId, campusId);
        
        return getEventById(eventId).orElseThrow(() -> 
            new RuntimeException("Failed to retrieve cancelled event"));
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
     * Get filtered list of future events.
     * 
     * @param campusId Optional campus filter
     * @param organizerId Optional organizer filter
     * @param startDate Optional start date filter (events starting on or after this date)
     * @param endDate Optional end date filter (events starting on or before this date)
     * @param freeOnly Optional filter for free events only
     * @param minPrice Optional minimum price filter
     * @param maxPrice Optional maximum price filter
     * @param tags Optional list of tag names to filter by
     * @return List of events matching the filters
     */
    public List<EventDTO> getEvents(Long campusId, Long organizerId, LocalDate startDate, LocalDate endDate,
                                     Boolean freeOnly, BigDecimal minPrice, BigDecimal maxPrice, List<String> tags) {
        // Build dynamic SQL query
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.id, e.organizer_id, e.campus_id, e.capacity, e.description, ");
        sql.append("e.start_time, e.end_time, o.name as organizer_name, c.name as campus_name ");
        sql.append("FROM event e ");
        sql.append("JOIN organization o ON e.organizer_id = o.id ");
        sql.append("JOIN campus c ON e.campus_id = c.id ");
        sql.append("WHERE 1=1 ");
        
        List<Object> params = new ArrayList<>();
        
        // Add optional filters
        if (campusId != null) {
            sql.append("AND e.campus_id = ? ");
            params.add(campusId);
        }
        
        if (organizerId != null) {
            sql.append("AND e.organizer_id = ? ");
            params.add(organizerId);
        }
        
        if (startDate != null) {
            sql.append("AND e.start_time >= ? ");
            params.add(Timestamp.valueOf(startDate.atStartOfDay()));
        }
        
        if (endDate != null) {
            sql.append("AND e.start_time <= ? ");
            params.add(Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
        }
        
        // Price filters using subqueries on the cost table
        if (Boolean.TRUE.equals(freeOnly)) {
            // Free events: events where the minimum cost is 0
            sql.append("AND EXISTS (SELECT 1 FROM cost co WHERE co.event_id = e.id AND co.cost = 0) ");
        } else {
            if (minPrice != null) {
                // Events with at least one ticket type >= minPrice
                sql.append("AND EXISTS (SELECT 1 FROM cost co WHERE co.event_id = e.id AND co.cost >= ?) ");
                params.add(minPrice);
            }
            if (maxPrice != null) {
                // Events with at least one ticket type <= maxPrice
                sql.append("AND EXISTS (SELECT 1 FROM cost co WHERE co.event_id = e.id AND co.cost <= ?) ");
                params.add(maxPrice);
            }
        }
        
        // Tag filter
        if (tags != null && !tags.isEmpty()) {
            sql.append("AND EXISTS (SELECT 1 FROM event_tag et ");
            sql.append("JOIN tag t ON et.tag_id = t.id ");
            sql.append("WHERE et.event_id = e.id AND LOWER(t.name) IN (");
            for (int i = 0; i < tags.size(); i++) {
                sql.append(i > 0 ? ", ?" : "?");
                params.add(tags.get(i).toLowerCase());
            }
            sql.append(")) ");
        }
        
        // Only show future events
        sql.append("AND e.start_time >= NOW() ");
        sql.append("ORDER BY e.start_time");
        
        List<Map<String, Object>> eventRows = sqlExecutor.executeQuery(sql.toString(), params.toArray());
        
        // Convert to EventDTOs with ticket counts
        return eventRows.stream()
            .map(this::mapRowToEventDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Map a database row to an EventDTO, including ticket count.
     */
    private EventDTO mapRowToEventDTO(Map<String, Object> eventRow) {
        Long eventId = ((Number) eventRow.get("id")).longValue();
        
        // Fetch costs for this event
        String costsSql = "SELECT type, cost FROM cost WHERE event_id = ?";
        List<Map<String, Object>> costsRows = sqlExecutor.executeQuery(costsSql, new Object[]{eventId});
        
        List<CostDTO> costs = costsRows.stream()
            .map(row -> new CostDTO(
                (String) row.get("type"),
                (BigDecimal) row.get("cost")
            ))
            .collect(Collectors.toList());
        
        // Fetch tickets sold count
        String ticketsSql = "SELECT COUNT(*) as tickets_sold FROM ticket WHERE event_id = ?";
        Map<String, Object> ticketsRow = sqlExecutor.executeQueryForMap(ticketsSql, new Object[]{eventId});
        Long ticketsSold = ticketsRow != null ? ((Number) ticketsRow.get("tickets_sold")).longValue() : 0L;
        
        // Fetch tags
        List<String> tags = getEventTags(eventId);
        
        // Build the EventDTO
        EventDTO eventDTO = new EventDTO();
        eventDTO.setId(eventId);
        eventDTO.setOrganizerId(((Number) eventRow.get("organizer_id")).longValue());
        eventDTO.setOrganizerName((String) eventRow.get("organizer_name"));
        eventDTO.setCampusId(((Number) eventRow.get("campus_id")).longValue());
        eventDTO.setCampusName((String) eventRow.get("campus_name"));
        eventDTO.setCapacity(((Number) eventRow.get("capacity")).intValue());
        eventDTO.setDescription((String) eventRow.get("description"));
        
        // Handle timestamp conversion
        Object startTimeObj = eventRow.get("start_time");
        Object endTimeObj = eventRow.get("end_time");
        
        if (startTimeObj instanceof Timestamp) {
            eventDTO.setStartTime(((Timestamp) startTimeObj).toLocalDateTime());
        } else if (startTimeObj instanceof LocalDateTime) {
            eventDTO.setStartTime((LocalDateTime) startTimeObj);
        }
        
        if (endTimeObj instanceof Timestamp) {
            eventDTO.setEndTime(((Timestamp) endTimeObj).toLocalDateTime());
        } else if (endTimeObj instanceof LocalDateTime) {
            eventDTO.setEndTime((LocalDateTime) endTimeObj);
        }
        
        eventDTO.setCosts(costs);
        eventDTO.setTicketsSold(ticketsSold);
        eventDTO.setAvailableCapacity(eventDTO.getCapacity() - ticketsSold.intValue());
        eventDTO.setTags(tags);
        
        return eventDTO;
    }
    
    /**
     * Get analytics for an event.
     * Includes ticket counts and revenue breakdown by type.
     * 
     * @param eventId The event ID
     * @return Analytics data for the event
     */
    public EventAnalyticsDTO getEventAnalytics(Long eventId) {
        // Get ticket counts and revenue by type
        String byTypeSql = "SELECT type, COUNT(*) as count, SUM(c.cost) as revenue " +
                          "FROM ticket t JOIN cost c ON t.event_id = c.event_id AND t.type = c.type " +
                          "WHERE t.event_id = ? GROUP BY type";
        
        List<Map<String, Object>> typeResults = sqlExecutor.executeQuery(byTypeSql, new Object[]{eventId});
        
        List<TicketTypeAnalyticsDTO> byType = typeResults.stream()
            .map(row -> new TicketTypeAnalyticsDTO(
                (String) row.get("type"),
                ((Number) row.get("count")).longValue(),
                row.get("revenue") != null ? new BigDecimal(row.get("revenue").toString()) : BigDecimal.ZERO
            ))
            .collect(Collectors.toList());
        
        // Get total tickets
        String totalSql = "SELECT COUNT(*) as total_tickets FROM ticket WHERE event_id = ?";
        Long totalTickets = sqlExecutor.executeScalar(totalSql, new Object[]{eventId}, Long.class);
        if (totalTickets == null) {
            totalTickets = 0L;
        }
        
        // Calculate total revenue
        BigDecimal totalRevenue = byType.stream()
            .map(TicketTypeAnalyticsDTO::getRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new EventAnalyticsDTO(eventId, totalTickets, totalRevenue, byType);
    }
    
    /**
     * Get all tags for an event.
     * 
     * @param eventId The event ID
     * @return List of tag names
     */
    public List<String> getEventTags(Long eventId) {
        String sql = "SELECT t.name FROM tag t " +
                     "JOIN event_tag et ON t.id = et.tag_id " +
                     "WHERE et.event_id = ? ORDER BY t.name";
        List<Map<String, Object>> rows = sqlExecutor.executeQuery(sql, new Object[]{eventId});
        return rows.stream()
            .map(row -> (String) row.get("name"))
            .collect(Collectors.toList());
    }
    
    /**
     * Save tags for an event.
     * Creates new tags if they don't exist.
     * 
     * @param eventId The event ID
     * @param tagNames List of tag names
     */
    private void saveEventTags(Long eventId, List<String> tagNames) {
        for (String tagName : tagNames) {
            if (tagName == null || tagName.trim().isEmpty()) {
                continue;
            }
            
            String trimmedName = tagName.trim();
            
            // Check if tag exists, if not create it
            String findTagSql = "SELECT id FROM tag WHERE LOWER(name) = LOWER(?)";
            Map<String, Object> existingTag = sqlExecutor.executeQueryForMap(findTagSql, new Object[]{trimmedName});
            
            Long tagId;
            if (existingTag != null) {
                tagId = ((Number) existingTag.get("id")).longValue();
            } else {
                // Create new tag
                tagId = sqlExecutor.executeInsert("INSERT INTO tag (name) VALUES (?)", new Object[]{trimmedName});
            }
            
            // Link tag to event (ignore if already exists)
            String linkSql = "INSERT INTO event_tag (event_id, tag_id) VALUES (?, ?) " +
                            "ON CONFLICT (event_id, tag_id) DO NOTHING";
            try {
                sqlExecutor.executeUpdate(linkSql, new Object[]{eventId, tagId});
            } catch (Exception e) {
                // In case DB doesn't support ON CONFLICT, try simple insert and ignore duplicate
                try {
                    String simpleLinkSql = "INSERT INTO event_tag (event_id, tag_id) VALUES (?, ?)";
                    sqlExecutor.executeUpdate(simpleLinkSql, new Object[]{eventId, tagId});
                } catch (Exception ignored) {
                    // Already exists, ignore
                }
            }
        }
    }
    
    /**
     * Get all available tags.
     * 
     * @return List of all tag names
     */
    public List<String> getAllTags() {
        String sql = "SELECT name FROM tag ORDER BY name";
        List<Map<String, Object>> rows = sqlExecutor.executeQuery(sql, new Object[]{});
        return rows.stream()
            .map(row -> (String) row.get("name"))
            .collect(Collectors.toList());
    }
}
