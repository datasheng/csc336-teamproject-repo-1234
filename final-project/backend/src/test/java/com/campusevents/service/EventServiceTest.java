package com.campusevents.service;

import com.campusevents.dto.CostDTO;
import com.campusevents.dto.CreateEventRequest;
import com.campusevents.dto.EventAnalyticsDTO;
import com.campusevents.dto.EventDTO;
import com.campusevents.dto.TicketTypeAnalyticsDTO;
import com.campusevents.dto.UpdateEventRequest;
import com.campusevents.util.SqlExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventService.
 * Tests event creation, retrieval, and update functionality with mocked dependencies.
 */
class EventServiceTest {
    
    private SqlExecutor sqlExecutor;
    private PubSubService pubSubService;
    private EventService eventService;
    
    @BeforeEach
    void setUp() {
        sqlExecutor = mock(SqlExecutor.class);
        pubSubService = mock(PubSubService.class);
        eventService = new EventService(sqlExecutor, pubSubService);
    }
    
    @Nested
    @DisplayName("Leadership Check Tests")
    class LeadershipCheckTests {
        
        @Test
        @DisplayName("Should return true when user is a leader")
        void shouldReturnTrueWhenUserIsLeader() {
            when(sqlExecutor.exists(
                eq("SELECT 1 FROM org_leadership WHERE user_id = ? AND org_id = ?"),
                eq(new Object[]{1L, 1L})
            )).thenReturn(true);
            
            assertTrue(eventService.isLeader(1L, 1L));
        }
        
        @Test
        @DisplayName("Should return false when user is not a leader")
        void shouldReturnFalseWhenUserIsNotLeader() {
            when(sqlExecutor.exists(
                eq("SELECT 1 FROM org_leadership WHERE user_id = ? AND org_id = ?"),
                eq(new Object[]{1L, 1L})
            )).thenReturn(false);
            
            assertFalse(eventService.isLeader(1L, 1L));
        }
    }
    
    @Nested
    @DisplayName("Create Event Tests")
    class CreateEventTests {
        
        @Test
        @DisplayName("Should create event successfully with costs")
        void shouldCreateEventSuccessfully() {
            // Arrange
            CreateEventRequest request = new CreateEventRequest();
            request.setOrganizerId(1L);
            request.setCampusId(2L);
            request.setCapacity(100);
            request.setDescription("Test Event");
            request.setStartTime(LocalDateTime.of(2024, 3, 15, 10, 0));
            request.setEndTime(LocalDateTime.of(2024, 3, 15, 18, 0));
            request.setCosts(Arrays.asList(
                new CostDTO("General", new BigDecimal("15.00")),
                new CostDTO("VIP", new BigDecimal("50.00"))
            ));
            
            // Mock insert returning event ID
            when(sqlExecutor.executeInsert(
                contains("INSERT INTO event"),
                any(Object[].class)
            )).thenReturn(10L);
            
            // Mock cost inserts
            when(sqlExecutor.executeUpdate(
                eq("INSERT INTO cost (type, event_id, cost) VALUES (?, ?, ?)"),
                any(Object[].class)
            )).thenReturn(1);
            
            // Mock event retrieval
            Map<String, Object> eventRow = createEventRow(10L, 1L, 2L, 100, "Test Event",
                LocalDateTime.of(2024, 3, 15, 10, 0), LocalDateTime.of(2024, 3, 15, 18, 0),
                "Test Org", "Test Campus");
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT e.id"),
                eq(new Object[]{10L})
            )).thenReturn(eventRow);
            
            // Mock costs retrieval
            List<Map<String, Object>> costsRows = Arrays.asList(
                createCostRow("General", new BigDecimal("15.00")),
                createCostRow("VIP", new BigDecimal("50.00"))
            );
            when(sqlExecutor.executeQuery(
                eq("SELECT type, cost FROM cost WHERE event_id = ?"),
                eq(new Object[]{10L})
            )).thenReturn(costsRows);
            
            // Mock tickets count
            Map<String, Object> ticketsRow = new HashMap<>();
            ticketsRow.put("tickets_sold", 0L);
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT COUNT(*)"),
                eq(new Object[]{10L})
            )).thenReturn(ticketsRow);
            
            // Act
            EventDTO result = eventService.createEvent(request);
            
            // Assert
            assertNotNull(result);
            assertEquals(10L, result.getId());
            assertEquals(1L, result.getOrganizerId());
            assertEquals(2L, result.getCampusId());
            assertEquals(100, result.getCapacity());
            assertEquals("Test Event", result.getDescription());
            assertEquals(2, result.getCosts().size());
            assertEquals(0L, result.getTicketsSold());
            assertEquals(100, result.getAvailableCapacity());
            
            // Verify Pub/Sub message was published
            verify(pubSubService).publishEventCreated(10L, 1L, 2L);
        }
        
        @Test
        @DisplayName("Should create event without costs")
        void shouldCreateEventWithoutCosts() {
            // Arrange
            CreateEventRequest request = new CreateEventRequest();
            request.setOrganizerId(1L);
            request.setCampusId(2L);
            request.setCapacity(50);
            request.setDescription("Free Event");
            request.setStartTime(LocalDateTime.of(2024, 4, 1, 14, 0));
            request.setEndTime(LocalDateTime.of(2024, 4, 1, 16, 0));
            request.setCosts(Collections.emptyList());
            
            when(sqlExecutor.executeInsert(
                contains("INSERT INTO event"),
                any(Object[].class)
            )).thenReturn(20L);
            
            Map<String, Object> eventRow = createEventRow(20L, 1L, 2L, 50, "Free Event",
                LocalDateTime.of(2024, 4, 1, 14, 0), LocalDateTime.of(2024, 4, 1, 16, 0),
                "Test Org", "Test Campus");
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT e.id"),
                eq(new Object[]{20L})
            )).thenReturn(eventRow);
            
            when(sqlExecutor.executeQuery(
                eq("SELECT type, cost FROM cost WHERE event_id = ?"),
                eq(new Object[]{20L})
            )).thenReturn(Collections.emptyList());
            
            Map<String, Object> ticketsRow = new HashMap<>();
            ticketsRow.put("tickets_sold", 0L);
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT COUNT(*)"),
                eq(new Object[]{20L})
            )).thenReturn(ticketsRow);
            
            // Act
            EventDTO result = eventService.createEvent(request);
            
            // Assert
            assertNotNull(result);
            assertEquals(20L, result.getId());
            assertTrue(result.getCosts().isEmpty());
            
            // Verify cost insert was never called
            verify(sqlExecutor, never()).executeUpdate(
                eq("INSERT INTO cost (type, event_id, cost) VALUES (?, ?, ?)"),
                any(Object[].class)
            );
        }
    }
    
    @Nested
    @DisplayName("Get Event Tests")
    class GetEventTests {
        
        @Test
        @DisplayName("Should return event with full details")
        void shouldReturnEventWithDetails() {
            // Arrange
            Map<String, Object> eventRow = createEventRow(1L, 2L, 3L, 200, "Tech Conference",
                LocalDateTime.of(2024, 5, 20, 9, 0), LocalDateTime.of(2024, 5, 20, 17, 0),
                "Tech Club", "Main Campus");
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT e.id"),
                eq(new Object[]{1L})
            )).thenReturn(eventRow);
            
            List<Map<String, Object>> costsRows = Arrays.asList(
                createCostRow("Student", new BigDecimal("10.00")),
                createCostRow("General", new BigDecimal("25.00"))
            );
            when(sqlExecutor.executeQuery(
                eq("SELECT type, cost FROM cost WHERE event_id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(costsRows);
            
            Map<String, Object> ticketsRow = new HashMap<>();
            ticketsRow.put("tickets_sold", 50L);
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT COUNT(*)"),
                eq(new Object[]{1L})
            )).thenReturn(ticketsRow);
            
            // Act
            Optional<EventDTO> result = eventService.getEventById(1L);
            
            // Assert
            assertTrue(result.isPresent());
            EventDTO event = result.get();
            assertEquals(1L, event.getId());
            assertEquals("Tech Club", event.getOrganizerName());
            assertEquals("Main Campus", event.getCampusName());
            assertEquals(200, event.getCapacity());
            assertEquals(50L, event.getTicketsSold());
            assertEquals(150, event.getAvailableCapacity());
            assertEquals(2, event.getCosts().size());
        }
        
        @Test
        @DisplayName("Should return empty when event not found")
        void shouldReturnEmptyWhenEventNotFound() {
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT e.id"),
                eq(new Object[]{999L})
            )).thenReturn(null);
            
            Optional<EventDTO> result = eventService.getEventById(999L);
            
            assertTrue(result.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Update Event Tests")
    class UpdateEventTests {
        
        @Test
        @DisplayName("Should update event successfully")
        void shouldUpdateEventSuccessfully() {
            // Arrange
            UpdateEventRequest request = new UpdateEventRequest();
            request.setCapacity(150);
            request.setDescription("Updated Description");
            request.setStartTime(LocalDateTime.of(2024, 3, 20, 10, 0));
            request.setEndTime(LocalDateTime.of(2024, 3, 20, 18, 0));
            
            when(sqlExecutor.executeUpdate(
                contains("UPDATE event SET"),
                any(Object[].class)
            )).thenReturn(1);
            
            Map<String, Object> eventRow = createEventRow(5L, 1L, 2L, 150, "Updated Description",
                LocalDateTime.of(2024, 3, 20, 10, 0), LocalDateTime.of(2024, 3, 20, 18, 0),
                "Test Org", "Test Campus");
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT e.id"),
                eq(new Object[]{5L})
            )).thenReturn(eventRow);
            
            when(sqlExecutor.executeQuery(
                eq("SELECT type, cost FROM cost WHERE event_id = ?"),
                eq(new Object[]{5L})
            )).thenReturn(Collections.emptyList());
            
            Map<String, Object> ticketsRow = new HashMap<>();
            ticketsRow.put("tickets_sold", 10L);
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT COUNT(*)"),
                eq(new Object[]{5L})
            )).thenReturn(ticketsRow);
            
            // Act
            EventDTO result = eventService.updateEvent(5L, request);
            
            // Assert
            assertNotNull(result);
            assertEquals(150, result.getCapacity());
            assertEquals("Updated Description", result.getDescription());
            
            // Verify Pub/Sub message was published
            verify(pubSubService).publishEventUpdated(5L);
        }
    }
    
    @Nested
    @DisplayName("Get Events List Tests")
    class GetEventsListTests {
        
        @Test
        @DisplayName("Should return events without filters")
        void shouldReturnEventsWithoutFilters() {
            // Arrange
            List<Map<String, Object>> eventRows = Arrays.asList(
                createEventRow(1L, 1L, 1L, 100, "Event 1",
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2),
                    "Org 1", "Campus 1"),
                createEventRow(2L, 2L, 1L, 50, "Event 2",
                    LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(3),
                    "Org 2", "Campus 1")
            );
            
            when(sqlExecutor.executeQuery(
                contains("WHERE 1=1"),
                any(Object[].class)
            )).thenReturn(eventRows);
            
            // Mock costs and tickets for each event
            when(sqlExecutor.executeQuery(
                eq("SELECT type, cost FROM cost WHERE event_id = ?"),
                any(Object[].class)
            )).thenReturn(Collections.emptyList());
            
            Map<String, Object> ticketsRow = new HashMap<>();
            ticketsRow.put("tickets_sold", 0L);
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT COUNT(*)"),
                any(Object[].class)
            )).thenReturn(ticketsRow);
            
            // Act
            List<EventDTO> result = eventService.getEvents(null, null, null, null, null, null, null);
            
            // Assert
            assertEquals(2, result.size());
            assertEquals("Event 1", result.get(0).getDescription());
            assertEquals("Event 2", result.get(1).getDescription());
        }
        
        @Test
        @DisplayName("Should filter events by campusId")
        void shouldFilterByCampusId() {
            // Arrange
            List<Map<String, Object>> eventRows = Arrays.asList(
                createEventRow(1L, 1L, 2L, 100, "Campus 2 Event",
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2),
                    "Org 1", "Campus 2")
            );
            
            when(sqlExecutor.executeQuery(
                argThat(sql -> sql != null && sql.contains("AND e.campus_id = ?")),
                any(Object[].class)
            )).thenReturn(eventRows);
            
            when(sqlExecutor.executeQuery(
                eq("SELECT type, cost FROM cost WHERE event_id = ?"),
                any(Object[].class)
            )).thenReturn(Collections.emptyList());
            
            Map<String, Object> ticketsRow = new HashMap<>();
            ticketsRow.put("tickets_sold", 0L);
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT COUNT(*)"),
                any(Object[].class)
            )).thenReturn(ticketsRow);
            
            // Act
            List<EventDTO> result = eventService.getEvents(2L, null, null, null, null, null, null);
            
            // Assert
            assertEquals(1, result.size());
            assertEquals(2L, result.get(0).getCampusId());
        }
        
        @Test
        @DisplayName("Should filter events by organizerId")
        void shouldFilterByOrganizerId() {
            // Arrange
            List<Map<String, Object>> eventRows = Arrays.asList(
                createEventRow(1L, 3L, 1L, 100, "Org 3 Event",
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2),
                    "Org 3", "Campus 1")
            );
            
            when(sqlExecutor.executeQuery(
                argThat(sql -> sql != null && sql.contains("AND e.organizer_id = ?")),
                any(Object[].class)
            )).thenReturn(eventRows);
            
            when(sqlExecutor.executeQuery(
                eq("SELECT type, cost FROM cost WHERE event_id = ?"),
                any(Object[].class)
            )).thenReturn(Collections.emptyList());
            
            Map<String, Object> ticketsRow = new HashMap<>();
            ticketsRow.put("tickets_sold", 0L);
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT COUNT(*)"),
                any(Object[].class)
            )).thenReturn(ticketsRow);
            
            // Act
            List<EventDTO> result = eventService.getEvents(null, 3L, null, null, null, null, null);
            
            // Assert
            assertEquals(1, result.size());
            assertEquals(3L, result.get(0).getOrganizerId());
        }
        
        @Test
        @DisplayName("Should filter events by date range")
        void shouldFilterByDateRange() {
            // Arrange
            LocalDate startDate = LocalDate.now().plusDays(5);
            LocalDate endDate = LocalDate.now().plusDays(10);
            
            List<Map<String, Object>> eventRows = Arrays.asList(
                createEventRow(1L, 1L, 1L, 100, "Event in range",
                    startDate.plusDays(2).atTime(10, 0), startDate.plusDays(2).atTime(12, 0),
                    "Org 1", "Campus 1")
            );
            
            when(sqlExecutor.executeQuery(
                argThat(sql -> sql != null && sql.contains("AND e.start_time >= ?") && sql.contains("AND e.start_time <= ?")),
                any(Object[].class)
            )).thenReturn(eventRows);
            
            when(sqlExecutor.executeQuery(
                eq("SELECT type, cost FROM cost WHERE event_id = ?"),
                any(Object[].class)
            )).thenReturn(Collections.emptyList());
            
            Map<String, Object> ticketsRow = new HashMap<>();
            ticketsRow.put("tickets_sold", 0L);
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT COUNT(*)"),
                any(Object[].class)
            )).thenReturn(ticketsRow);
            
            // Act
            List<EventDTO> result = eventService.getEvents(null, null, startDate, endDate, null, null, null);
            
            // Assert
            assertEquals(1, result.size());
        }
        
        @Test
        @DisplayName("Should return empty list when no events match")
        void shouldReturnEmptyListWhenNoEventsMatch() {
            when(sqlExecutor.executeQuery(
                anyString(),
                any(Object[].class)
            )).thenReturn(Collections.emptyList());
            
            List<EventDTO> result = eventService.getEvents(999L, null, null, null, null, null, null);
            
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should include availability in results")
        void shouldIncludeAvailabilityInResults() {
            // Arrange
            List<Map<String, Object>> eventRows = Arrays.asList(
                createEventRow(1L, 1L, 1L, 100, "Event with tickets",
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2),
                    "Org 1", "Campus 1")
            );
            
            when(sqlExecutor.executeQuery(
                contains("WHERE 1=1"),
                any(Object[].class)
            )).thenReturn(eventRows);
            
            when(sqlExecutor.executeQuery(
                eq("SELECT type, cost FROM cost WHERE event_id = ?"),
                any(Object[].class)
            )).thenReturn(Collections.emptyList());
            
            Map<String, Object> ticketsRow = new HashMap<>();
            ticketsRow.put("tickets_sold", 30L);
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT COUNT(*)"),
                any(Object[].class)
            )).thenReturn(ticketsRow);
            
            // Act
            List<EventDTO> result = eventService.getEvents(null, null, null, null, null, null, null);
            
            // Assert
            assertEquals(1, result.size());
            assertEquals(100, result.get(0).getCapacity());
            assertEquals(30L, result.get(0).getTicketsSold());
            assertEquals(70, result.get(0).getAvailableCapacity());
        }
    }
    
    // Helper methods
    private Map<String, Object> createEventRow(Long id, Long organizerId, Long campusId,
            int capacity, String description, LocalDateTime startTime, LocalDateTime endTime,
            String organizerName, String campusName) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("organizer_id", organizerId);
        row.put("campus_id", campusId);
        row.put("capacity", capacity);
        row.put("description", description);
        row.put("start_time", Timestamp.valueOf(startTime));
        row.put("end_time", Timestamp.valueOf(endTime));
        row.put("organizer_name", organizerName);
        row.put("campus_name", campusName);
        return row;
    }
    
    private Map<String, Object> createCostRow(String type, BigDecimal cost) {
        Map<String, Object> row = new HashMap<>();
        row.put("type", type);
        row.put("cost", cost);
        return row;
    }
    
    @Nested
    @DisplayName("Get Event Analytics Tests")
    class GetEventAnalyticsTests {
        
        @Test
        @DisplayName("Should return analytics with ticket breakdown by type")
        void shouldReturnAnalyticsWithBreakdownByType() {
            // Arrange
            Long eventId = 1L;
            
            Map<String, Object> generalRow = new HashMap<>();
            generalRow.put("type", "general");
            generalRow.put("count", 50L);
            generalRow.put("revenue", new BigDecimal("500.00"));
            
            Map<String, Object> vipRow = new HashMap<>();
            vipRow.put("type", "vip");
            vipRow.put("count", 10L);
            vipRow.put("revenue", new BigDecimal("250.00"));
            
            when(sqlExecutor.executeQuery(
                argThat(sql -> sql != null && sql.contains("GROUP BY type")),
                eq(new Object[]{eventId})
            )).thenReturn(Arrays.asList(generalRow, vipRow));
            
            when(sqlExecutor.executeScalar(
                argThat(sql -> sql != null && sql.contains("COUNT(*) as total_tickets")),
                eq(new Object[]{eventId}),
                eq(Long.class)
            )).thenReturn(60L);
            
            // Act
            EventAnalyticsDTO analytics = eventService.getEventAnalytics(eventId);
            
            // Assert
            assertNotNull(analytics);
            assertEquals(eventId, analytics.getEventId());
            assertEquals(60L, analytics.getTotalTickets());
            assertEquals(new BigDecimal("750.00"), analytics.getTotalRevenue());
            assertEquals(2, analytics.getByType().size());
            
            TicketTypeAnalyticsDTO general = analytics.getByType().stream()
                .filter(t -> "general".equals(t.getType()))
                .findFirst().orElseThrow();
            assertEquals(50L, general.getCount());
            assertEquals(new BigDecimal("500.00"), general.getRevenue());
        }
        
        @Test
        @DisplayName("Should return zero analytics when no tickets sold")
        void shouldReturnZeroAnalyticsWhenNoTicketsSold() {
            // Arrange
            Long eventId = 1L;
            
            when(sqlExecutor.executeQuery(
                argThat(sql -> sql != null && sql.contains("GROUP BY type")),
                eq(new Object[]{eventId})
            )).thenReturn(Collections.emptyList());
            
            when(sqlExecutor.executeScalar(
                argThat(sql -> sql != null && sql.contains("COUNT(*) as total_tickets")),
                eq(new Object[]{eventId}),
                eq(Long.class)
            )).thenReturn(0L);
            
            // Act
            EventAnalyticsDTO analytics = eventService.getEventAnalytics(eventId);
            
            // Assert
            assertNotNull(analytics);
            assertEquals(0L, analytics.getTotalTickets());
            assertEquals(BigDecimal.ZERO, analytics.getTotalRevenue());
            assertTrue(analytics.getByType().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Pub/Sub Integration Tests")
    class PubSubIntegrationTests {
        
        @Test
        @DisplayName("Should publish EVENT_CREATED after creating event")
        void shouldPublishEventCreatedAfterCreating() {
            CreateEventRequest request = new CreateEventRequest();
            request.setOrganizerId(1L);
            request.setCampusId(2L);
            request.setCapacity(100);
            request.setDescription("Pub/Sub Test Event");
            request.setStartTime(LocalDateTime.of(2024, 6, 1, 10, 0));
            request.setEndTime(LocalDateTime.of(2024, 6, 1, 18, 0));
            request.setCosts(Collections.emptyList());
            
            when(sqlExecutor.executeInsert(contains("INSERT INTO event"), any(Object[].class)))
                .thenReturn(42L);
            
            Map<String, Object> eventRow = createEventRow(42L, 1L, 2L, 100, "Pub/Sub Test Event",
                LocalDateTime.of(2024, 6, 1, 10, 0), LocalDateTime.of(2024, 6, 1, 18, 0),
                "Test Org", "Test Campus");
            when(sqlExecutor.executeQueryForMap(contains("SELECT e.id"), eq(new Object[]{42L})))
                .thenReturn(eventRow);
            when(sqlExecutor.executeQuery(eq("SELECT type, cost FROM cost WHERE event_id = ?"), eq(new Object[]{42L})))
                .thenReturn(Collections.emptyList());
            
            Map<String, Object> ticketsRow = new HashMap<>();
            ticketsRow.put("tickets_sold", 0L);
            when(sqlExecutor.executeQueryForMap(eq("SELECT COUNT(*) as tickets_sold FROM ticket WHERE event_id = ?"), eq(new Object[]{42L})))
                .thenReturn(ticketsRow);
            
            eventService.createEvent(request);
            
            verify(pubSubService, times(1)).publishEventCreated(42L, 1L, 2L);
        }
        
        @Test
        @DisplayName("Should publish EVENT_UPDATED after updating event")
        void shouldPublishEventUpdatedAfterUpdating() {
            UpdateEventRequest request = new UpdateEventRequest();
            request.setDescription("Updated Event");
            request.setCapacity(100);
            request.setStartTime(LocalDateTime.of(2024, 6, 1, 10, 0));
            request.setEndTime(LocalDateTime.of(2024, 6, 1, 18, 0));
            
            // Setup mocks for update
            when(sqlExecutor.executeUpdate(contains("UPDATE event"), any(Object[].class)))
                .thenReturn(1);
            
            Map<String, Object> eventRow = createEventRow(5L, 1L, 2L, 100, "Updated Event",
                LocalDateTime.of(2024, 6, 1, 10, 0), LocalDateTime.of(2024, 6, 1, 18, 0),
                "Test Org", "Test Campus");
            when(sqlExecutor.executeQueryForMap(contains("SELECT e.id"), eq(new Object[]{5L})))
                .thenReturn(eventRow);
            when(sqlExecutor.executeQuery(eq("SELECT type, cost FROM cost WHERE event_id = ?"), eq(new Object[]{5L})))
                .thenReturn(Collections.emptyList());
            
            Map<String, Object> ticketsRow = new HashMap<>();
            ticketsRow.put("tickets_sold", 10L);
            when(sqlExecutor.executeQueryForMap(eq("SELECT COUNT(*) as tickets_sold FROM ticket WHERE event_id = ?"), eq(new Object[]{5L})))
                .thenReturn(ticketsRow);
            
            eventService.updateEvent(5L, request);
            
            verify(pubSubService, times(1)).publishEventUpdated(5L);
        }
        
        @Test
        @DisplayName("Should not publish when event not found in update")
        void shouldNotPublishWhenEventNotFoundInUpdate() {
            UpdateEventRequest request = new UpdateEventRequest();
            request.setDescription("Non-existent");
            
            when(sqlExecutor.executeUpdate(contains("UPDATE event"), any(Object[].class)))
                .thenReturn(0);
            
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> eventService.updateEvent(999L, request));
            
            verify(pubSubService, never()).publishEventUpdated(anyLong());
        }
    }
    
    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle null description in event")
        void shouldHandleNullDescription() {
            CreateEventRequest request = new CreateEventRequest();
            request.setOrganizerId(1L);
            request.setCampusId(2L);
            request.setCapacity(50);
            request.setDescription(null);
            request.setStartTime(LocalDateTime.of(2024, 6, 1, 10, 0));
            request.setEndTime(LocalDateTime.of(2024, 6, 1, 18, 0));
            request.setCosts(Collections.emptyList());
            
            when(sqlExecutor.executeInsert(contains("INSERT INTO event"), any(Object[].class)))
                .thenReturn(1L);
            
            Map<String, Object> eventRow = createEventRow(1L, 1L, 2L, 50, null,
                LocalDateTime.of(2024, 6, 1, 10, 0), LocalDateTime.of(2024, 6, 1, 18, 0),
                "Test Org", "Test Campus");
            when(sqlExecutor.executeQueryForMap(contains("SELECT e.id"), eq(new Object[]{1L})))
                .thenReturn(eventRow);
            when(sqlExecutor.executeQuery(eq("SELECT type, cost FROM cost WHERE event_id = ?"), eq(new Object[]{1L})))
                .thenReturn(Collections.emptyList());
            
            Map<String, Object> ticketsRow = new HashMap<>();
            ticketsRow.put("tickets_sold", 0L);
            when(sqlExecutor.executeQueryForMap(eq("SELECT COUNT(*) as tickets_sold FROM ticket WHERE event_id = ?"), eq(new Object[]{1L})))
                .thenReturn(ticketsRow);
            
            EventDTO result = eventService.createEvent(request);
            
            assertNotNull(result);
            assertNull(result.getDescription());
        }
        
        @Test
        @DisplayName("Should handle zero capacity event")
        void shouldHandleZeroCapacity() {
            Map<String, Object> eventRow = createEventRow(1L, 1L, 2L, 0, "Zero Capacity Event",
                LocalDateTime.of(2024, 6, 1, 10, 0), LocalDateTime.of(2024, 6, 1, 18, 0),
                "Test Org", "Test Campus");
            when(sqlExecutor.executeQueryForMap(contains("SELECT e.id"), eq(new Object[]{1L})))
                .thenReturn(eventRow);
            when(sqlExecutor.executeQuery(eq("SELECT type, cost FROM cost WHERE event_id = ?"), eq(new Object[]{1L})))
                .thenReturn(Collections.emptyList());
            
            Map<String, Object> ticketsRow = new HashMap<>();
            ticketsRow.put("tickets_sold", 0L);
            when(sqlExecutor.executeQueryForMap(eq("SELECT COUNT(*) as tickets_sold FROM ticket WHERE event_id = ?"), eq(new Object[]{1L})))
                .thenReturn(ticketsRow);
            
            Optional<EventDTO> result = eventService.getEventById(1L);
            
            assertTrue(result.isPresent());
            assertEquals(0, result.get().getCapacity());
            assertEquals(0, result.get().getAvailableCapacity());
        }
        
        @Test
        @DisplayName("Should handle very large ticket count")
        void shouldHandleLargeTicketCount() {
            Map<String, Object> eventRow = createEventRow(1L, 1L, 2L, 10000, "Big Event",
                LocalDateTime.of(2024, 6, 1, 10, 0), LocalDateTime.of(2024, 6, 1, 18, 0),
                "Test Org", "Test Campus");
            when(sqlExecutor.executeQueryForMap(contains("SELECT e.id"), eq(new Object[]{1L})))
                .thenReturn(eventRow);
            when(sqlExecutor.executeQuery(eq("SELECT type, cost FROM cost WHERE event_id = ?"), eq(new Object[]{1L})))
                .thenReturn(Collections.emptyList());
            
            Map<String, Object> ticketsRow = new HashMap<>();
            ticketsRow.put("tickets_sold", 9999L);
            when(sqlExecutor.executeQueryForMap(eq("SELECT COUNT(*) as tickets_sold FROM ticket WHERE event_id = ?"), eq(new Object[]{1L})))
                .thenReturn(ticketsRow);
            
            Optional<EventDTO> result = eventService.getEventById(1L);
            
            assertTrue(result.isPresent());
            assertEquals(10000, result.get().getCapacity());
            assertEquals(9999L, result.get().getTicketsSold());
            assertEquals(1, result.get().getAvailableCapacity());
        }
    }
}
