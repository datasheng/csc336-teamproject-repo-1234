package com.campusevents.controller;

import com.campusevents.dto.CreateEventRequest;
import com.campusevents.dto.ErrorResponse;
import com.campusevents.dto.EventAnalyticsDTO;
import com.campusevents.dto.EventDTO;
import com.campusevents.dto.UpdateEventRequest;
import com.campusevents.model.User;
import com.campusevents.security.CurrentUser;
import com.campusevents.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for event management endpoints.
 * 
 * Endpoints:
 * - GET /api/events - List future events with optional filters
 * - POST /api/events - Create a new event (authenticated, must be org leader)
 * - GET /api/events/{id} - Get event details with availability
 * - PUT /api/events/{id} - Update an event (authenticated, must be org leader)
 * - GET /api/events/{id}/analytics - Get event analytics (authenticated, must be org leader)
 */
@RestController
@RequestMapping("/api/events")
public class EventController {
    
    private final EventService eventService;
    
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }
    
    /**
     * List future events with optional filters.
     * 
     * Query parameters:
     * - campusId: Filter by campus
     * - organizerId: Filter by organizing organization
     * - startDate: Filter events starting on or after this date (YYYY-MM-DD)
     * - endDate: Filter events starting on or before this date (YYYY-MM-DD)
     * - freeOnly: Filter to only show free events (boolean)
     * - minPrice: Filter events with at least one ticket type at or above this price
     * - maxPrice: Filter events with at least one ticket type at or below this price
     * 
     * @param campusId Optional campus filter
     * @param organizerId Optional organizer filter
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @param freeOnly Optional filter for free events only
     * @param minPrice Optional minimum price filter
     * @param maxPrice Optional maximum price filter
     * @return List of future events matching the filters
     */
    @GetMapping
    public ResponseEntity<?> getEvents(
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) Long organizerId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Boolean freeOnly,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        try {
            List<EventDTO> events = eventService.getEvents(campusId, organizerId, startDate, endDate, freeOnly, minPrice, maxPrice);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while fetching events", 500)
            );
        }
    }
    
    /**
     * Create a new event.
     * Only organization leaders can create events for their organization.
     * 
     * Request body:
     * {
     *   "organizerId": 1,
     *   "campusId": 1,
     *   "capacity": 100,
     *   "description": "Annual tech meetup",
     *   "startTime": "2024-03-15T10:00:00",
     *   "endTime": "2024-03-15T18:00:00",
     *   "costs": [
     *     {"type": "General", "cost": 15.00},
     *     {"type": "VIP", "cost": 50.00}
     *   ]
     * }
     * 
     * @param user The authenticated user (injected by @CurrentUser)
     * @param request The event creation request
     * @return Created event with ID
     */
    @PostMapping
    public ResponseEntity<?> createEvent(
            @CurrentUser User user,
            @RequestBody CreateEventRequest request) {
        try {
            // Validate required fields
            if (request.getOrganizerId() == null) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "Organizer ID is required", 400)
                );
            }
            if (request.getCampusId() == null) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "Campus ID is required", 400)
                );
            }
            if (request.getCapacity() == null || request.getCapacity() <= 0) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "Valid capacity is required", 400)
                );
            }
            if (request.getStartTime() == null) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "Start time is required", 400)
                );
            }
            if (request.getEndTime() == null) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "End time is required", 400)
                );
            }
            if (request.getEndTime().isBefore(request.getStartTime())) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "End time must be after start time", 400)
                );
            }
            
            // Check if organization exists
            if (!eventService.organizationExists(request.getOrganizerId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "Organization not found", 404)
                );
            }
            
            // Check if campus exists
            if (!eventService.campusExists(request.getCampusId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "Campus not found", 404)
                );
            }
            
            // Verify user is a leader of the organization
            if (!eventService.isLeader(user.getId(), request.getOrganizerId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse("Forbidden", "You must be a leader of the organization to create events", 403)
                );
            }
            
            // Create the event
            EventDTO createdEvent = eventService.createEvent(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while creating the event", 500)
            );
        }
    }
    
    /**
     * Get event details by ID.
     * Includes organizer and campus names, costs, and availability.
     * 
     * @param id The event ID
     * @return Event details with costs and availability
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEvent(@PathVariable Long id) {
        try {
            Optional<EventDTO> eventOpt = eventService.getEventById(id);
            
            if (eventOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "Event not found", 404)
                );
            }
            
            return ResponseEntity.ok(eventOpt.get());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while fetching the event", 500)
            );
        }
    }
    
    /**
     * Update an existing event.
     * Only organization leaders can update events for their organization.
     * 
     * Request body:
     * {
     *   "capacity": 150,
     *   "description": "Updated description",
     *   "startTime": "2024-03-15T09:00:00",
     *   "endTime": "2024-03-15T19:00:00"
     * }
     * 
     * @param user The authenticated user (injected by @CurrentUser)
     * @param id The event ID to update
     * @param request The update request
     * @return Updated event details
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @CurrentUser User user,
            @PathVariable Long id,
            @RequestBody UpdateEventRequest request) {
        try {
            // Validate required fields
            if (request.getCapacity() == null || request.getCapacity() <= 0) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "Valid capacity is required", 400)
                );
            }
            if (request.getStartTime() == null) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "Start time is required", 400)
                );
            }
            if (request.getEndTime() == null) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "End time is required", 400)
                );
            }
            if (request.getEndTime().isBefore(request.getStartTime())) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "End time must be after start time", 400)
                );
            }
            
            // Check if event exists
            if (!eventService.eventExists(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "Event not found", 404)
                );
            }
            
            // Get the organizer ID for the event
            Optional<Long> organizerIdOpt = eventService.getEventOrganizerId(id);
            if (organizerIdOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "Event not found", 404)
                );
            }
            
            Long organizerId = organizerIdOpt.get();
            
            // Verify user is a leader of the organization
            if (!eventService.isLeader(user.getId(), organizerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse("Forbidden", "You must be a leader of the organization to update events", 403)
                );
            }
            
            // Update the event
            EventDTO updatedEvent = eventService.updateEvent(id, request);
            return ResponseEntity.ok(updatedEvent);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while updating the event", 500)
            );
        }
    }
    
    /**
     * Get analytics for an event.
     * Only organization leaders can view analytics for their events.
     * 
     * @param id The event ID
     * @param user The authenticated user
     * @return Analytics data including ticket counts and revenue by type
     */
    @GetMapping("/{id}/analytics")
    public ResponseEntity<?> getEventAnalytics(
            @PathVariable Long id,
            @CurrentUser User user) {
        try {
            // Check if user is authenticated
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ErrorResponse("Unauthorized", "You must be logged in to view event analytics", 401)
                );
            }
            
            // Get the event's organization ID
            Optional<Long> organizerIdOpt = eventService.getEventOrganizerId(id);
            if (organizerIdOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", "Event not found", 404)
                );
            }
            Long organizerId = organizerIdOpt.get();
            
            // Check if user is a leader of the organization
            if (!eventService.isLeader(user.getId(), organizerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse("Forbidden", "You must be a leader of the organization to view event analytics", 403)
                );
            }
            
            // Get analytics
            EventAnalyticsDTO analytics = eventService.getEventAnalytics(id);
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while fetching analytics", 500)
            );
        }
    }
}
