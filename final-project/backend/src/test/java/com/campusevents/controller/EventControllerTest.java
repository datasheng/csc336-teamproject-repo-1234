package com.campusevents.controller;

import com.campusevents.dto.CostDTO;
import com.campusevents.dto.CreateEventRequest;
import com.campusevents.dto.EventAnalyticsDTO;
import com.campusevents.dto.EventDTO;
import com.campusevents.dto.TicketTypeAnalyticsDTO;
import com.campusevents.dto.UpdateEventRequest;
import com.campusevents.model.User;
import com.campusevents.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventController.
 * Tests REST endpoint handling with mocked EventService.
 */
class EventControllerTest {
    
    private EventService eventService;
    private EventController eventController;
    private User authenticatedUser;
    
    @BeforeEach
    void setUp() {
        eventService = mock(EventService.class);
        eventController = new EventController(eventService);
        
        // Create a test user
        authenticatedUser = new User(1L, "John", "Doe", "john@example.com", "hashedPassword", 1L);
    }
    
    @Nested
    @DisplayName("Create Event Tests")
    class CreateEventTests {
        
        @Test
        @DisplayName("Should create event successfully")
        void shouldCreateEventSuccessfully() {
            // Arrange
            CreateEventRequest request = new CreateEventRequest();
            request.setOrganizerId(1L);
            request.setCampusId(2L);
            request.setCapacity(100);
            request.setDescription("Test Event");
            request.setStartTime(LocalDateTime.of(2024, 3, 15, 10, 0));
            request.setEndTime(LocalDateTime.of(2024, 3, 15, 18, 0));
            request.setCosts(Arrays.asList(new CostDTO("General", new BigDecimal("15.00"))));
            
            EventDTO createdEvent = new EventDTO();
            createdEvent.setId(10L);
            createdEvent.setOrganizerId(1L);
            createdEvent.setCapacity(100);
            
            when(eventService.organizationExists(1L)).thenReturn(true);
            when(eventService.campusExists(2L)).thenReturn(true);
            when(eventService.isLeader(1L, 1L)).thenReturn(true);
            when(eventService.createEvent(request)).thenReturn(createdEvent);
            
            // Act
            ResponseEntity<?> response = eventController.createEvent(authenticatedUser, request);
            
            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof EventDTO);
            assertEquals(10L, ((EventDTO) response.getBody()).getId());
        }
        
        @Test
        @DisplayName("Should reject when organizer ID is missing")
        void shouldRejectMissingOrganizerId() {
            CreateEventRequest request = new CreateEventRequest();
            request.setCampusId(2L);
            request.setCapacity(100);
            request.setStartTime(LocalDateTime.of(2024, 3, 15, 10, 0));
            request.setEndTime(LocalDateTime.of(2024, 3, 15, 18, 0));
            
            ResponseEntity<?> response = eventController.createEvent(authenticatedUser, request);
            
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should reject when capacity is invalid")
        void shouldRejectInvalidCapacity() {
            CreateEventRequest request = new CreateEventRequest();
            request.setOrganizerId(1L);
            request.setCampusId(2L);
            request.setCapacity(0);
            request.setStartTime(LocalDateTime.of(2024, 3, 15, 10, 0));
            request.setEndTime(LocalDateTime.of(2024, 3, 15, 18, 0));
            
            ResponseEntity<?> response = eventController.createEvent(authenticatedUser, request);
            
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should reject when end time is before start time")
        void shouldRejectEndTimeBeforeStartTime() {
            CreateEventRequest request = new CreateEventRequest();
            request.setOrganizerId(1L);
            request.setCampusId(2L);
            request.setCapacity(100);
            request.setStartTime(LocalDateTime.of(2024, 3, 15, 18, 0));
            request.setEndTime(LocalDateTime.of(2024, 3, 15, 10, 0));
            
            ResponseEntity<?> response = eventController.createEvent(authenticatedUser, request);
            
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should reject when organization not found")
        void shouldRejectWhenOrganizationNotFound() {
            CreateEventRequest request = new CreateEventRequest();
            request.setOrganizerId(999L);
            request.setCampusId(2L);
            request.setCapacity(100);
            request.setStartTime(LocalDateTime.of(2024, 3, 15, 10, 0));
            request.setEndTime(LocalDateTime.of(2024, 3, 15, 18, 0));
            
            when(eventService.organizationExists(999L)).thenReturn(false);
            
            ResponseEntity<?> response = eventController.createEvent(authenticatedUser, request);
            
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should reject when user is not a leader")
        void shouldRejectWhenUserIsNotLeader() {
            CreateEventRequest request = new CreateEventRequest();
            request.setOrganizerId(1L);
            request.setCampusId(2L);
            request.setCapacity(100);
            request.setStartTime(LocalDateTime.of(2024, 3, 15, 10, 0));
            request.setEndTime(LocalDateTime.of(2024, 3, 15, 18, 0));
            
            when(eventService.organizationExists(1L)).thenReturn(true);
            when(eventService.campusExists(2L)).thenReturn(true);
            when(eventService.isLeader(1L, 1L)).thenReturn(false);
            
            ResponseEntity<?> response = eventController.createEvent(authenticatedUser, request);
            
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }
    }
    
    @Nested
    @DisplayName("Get Event Tests")
    class GetEventTests {
        
        @Test
        @DisplayName("Should get event successfully")
        void shouldGetEventSuccessfully() {
            EventDTO event = new EventDTO();
            event.setId(1L);
            event.setDescription("Test Event");
            event.setCapacity(100);
            event.setTicketsSold(25L);
            event.setAvailableCapacity(75);
            
            when(eventService.getEventById(1L)).thenReturn(Optional.of(event));
            
            ResponseEntity<?> response = eventController.getEvent(1L);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof EventDTO);
            assertEquals(1L, ((EventDTO) response.getBody()).getId());
            assertEquals(75, ((EventDTO) response.getBody()).getAvailableCapacity());
        }
        
        @Test
        @DisplayName("Should return 404 when event not found")
        void shouldReturn404WhenEventNotFound() {
            when(eventService.getEventById(999L)).thenReturn(Optional.empty());
            
            ResponseEntity<?> response = eventController.getEvent(999L);
            
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }
    
    @Nested
    @DisplayName("Update Event Tests")
    class UpdateEventTests {
        
        @Test
        @DisplayName("Should update event successfully")
        void shouldUpdateEventSuccessfully() {
            UpdateEventRequest request = new UpdateEventRequest();
            request.setCapacity(150);
            request.setDescription("Updated Event");
            request.setStartTime(LocalDateTime.of(2024, 3, 20, 10, 0));
            request.setEndTime(LocalDateTime.of(2024, 3, 20, 18, 0));
            
            EventDTO updatedEvent = new EventDTO();
            updatedEvent.setId(5L);
            updatedEvent.setCapacity(150);
            updatedEvent.setDescription("Updated Event");
            
            when(eventService.eventExists(5L)).thenReturn(true);
            when(eventService.getEventOrganizerId(5L)).thenReturn(Optional.of(1L));
            when(eventService.isLeader(1L, 1L)).thenReturn(true);
            when(eventService.updateEvent(eq(5L), any(UpdateEventRequest.class))).thenReturn(updatedEvent);
            
            ResponseEntity<?> response = eventController.updateEvent(authenticatedUser, 5L, request);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof EventDTO);
            assertEquals(150, ((EventDTO) response.getBody()).getCapacity());
        }
        
        @Test
        @DisplayName("Should reject when event not found")
        void shouldRejectWhenEventNotFound() {
            UpdateEventRequest request = new UpdateEventRequest();
            request.setCapacity(150);
            request.setDescription("Updated Event");
            request.setStartTime(LocalDateTime.of(2024, 3, 20, 10, 0));
            request.setEndTime(LocalDateTime.of(2024, 3, 20, 18, 0));
            
            when(eventService.eventExists(999L)).thenReturn(false);
            
            ResponseEntity<?> response = eventController.updateEvent(authenticatedUser, 999L, request);
            
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should reject when user is not leader of event organization")
        void shouldRejectWhenUserIsNotLeaderOfEventOrganization() {
            UpdateEventRequest request = new UpdateEventRequest();
            request.setCapacity(150);
            request.setDescription("Updated Event");
            request.setStartTime(LocalDateTime.of(2024, 3, 20, 10, 0));
            request.setEndTime(LocalDateTime.of(2024, 3, 20, 18, 0));
            
            when(eventService.eventExists(5L)).thenReturn(true);
            when(eventService.getEventOrganizerId(5L)).thenReturn(Optional.of(2L)); // Different org
            when(eventService.isLeader(1L, 2L)).thenReturn(false); // User not leader of org 2
            
            ResponseEntity<?> response = eventController.updateEvent(authenticatedUser, 5L, request);
            
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }
    }
    
    @Nested
    @DisplayName("List Events Tests")
    class ListEventsTests {
        
        @Test
        @DisplayName("Should list events without filters")
        void shouldListEventsWithoutFilters() {
            // Arrange
            EventDTO event1 = new EventDTO();
            event1.setId(1L);
            event1.setDescription("Event 1");
            event1.setCapacity(100);
            event1.setAvailableCapacity(80);
            
            EventDTO event2 = new EventDTO();
            event2.setId(2L);
            event2.setDescription("Event 2");
            event2.setCapacity(50);
            event2.setAvailableCapacity(50);
            
            when(eventService.getEvents(null, null, null, null, null, null, null, null))
                .thenReturn(Arrays.asList(event1, event2));
            
            // Act
            ResponseEntity<?> response = eventController.getEvents(null, null, null, null, null, null, null, null);
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody() instanceof List);
            List<?> events = (List<?>) response.getBody();
            assertEquals(2, events.size());
        }
        
        @Test
        @DisplayName("Should list events with campusId filter")
        void shouldListEventsWithCampusIdFilter() {
            EventDTO event = new EventDTO();
            event.setId(1L);
            event.setCampusId(2L);
            
            when(eventService.getEvents(2L, null, null, null, null, null, null, null))
                .thenReturn(Arrays.asList(event));
            
            ResponseEntity<?> response = eventController.getEvents(2L, null, null, null, null, null, null, null);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<?> events = (List<?>) response.getBody();
            assertEquals(1, events.size());
            
            verify(eventService).getEvents(2L, null, null, null, null, null, null, null);
        }
        
        @Test
        @DisplayName("Should list events with organizerId filter")
        void shouldListEventsWithOrganizerIdFilter() {
            EventDTO event = new EventDTO();
            event.setId(1L);
            event.setOrganizerId(3L);
            
            when(eventService.getEvents(null, 3L, null, null, null, null, null, null))
                .thenReturn(Arrays.asList(event));
            
            ResponseEntity<?> response = eventController.getEvents(null, 3L, null, null, null, null, null, null);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(eventService).getEvents(null, 3L, null, null, null, null, null, null);
        }
        
        @Test
        @DisplayName("Should list events with date filters")
        void shouldListEventsWithDateFilters() {
            LocalDate startDate = LocalDate.of(2024, 3, 1);
            LocalDate endDate = LocalDate.of(2024, 3, 31);
            
            when(eventService.getEvents(null, null, startDate, endDate, null, null, null, null))
                .thenReturn(Collections.emptyList());
            
            ResponseEntity<?> response = eventController.getEvents(null, null, startDate, endDate, null, null, null, null);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(eventService).getEvents(null, null, startDate, endDate, null, null, null, null);
        }
        
        @Test
        @DisplayName("Should list events with all filters")
        void shouldListEventsWithAllFilters() {
            LocalDate startDate = LocalDate.of(2024, 3, 1);
            LocalDate endDate = LocalDate.of(2024, 3, 31);
            
            when(eventService.getEvents(1L, 2L, startDate, endDate, null, null, null, null))
                .thenReturn(Collections.emptyList());
            
            ResponseEntity<?> response = eventController.getEvents(1L, 2L, startDate, endDate, null, null, null, null);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(eventService).getEvents(1L, 2L, startDate, endDate, null, null, null, null);
        }
        
        @Test
        @DisplayName("Should return empty list when no events match")
        void shouldReturnEmptyListWhenNoEventsMatch() {
            when(eventService.getEvents(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
            
            ResponseEntity<?> response = eventController.getEvents(999L, null, null, null, null, null, null, null);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<?> events = (List<?>) response.getBody();
            assertTrue(events.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Get Event Analytics Tests")
    class GetEventAnalyticsTests {
        
        @Test
        @DisplayName("Should return analytics for authorized org leader")
        void shouldReturnAnalyticsForAuthorizedOrgLeader() {
            Long eventId = 1L;
            Long organizerId = 1L;
            
            EventAnalyticsDTO analytics = new EventAnalyticsDTO(
                eventId, 60L, new BigDecimal("750.00"),
                Arrays.asList(
                    new TicketTypeAnalyticsDTO("general", 50L, new BigDecimal("500.00")),
                    new TicketTypeAnalyticsDTO("vip", 10L, new BigDecimal("250.00"))
                )
            );
            
            when(eventService.getEventOrganizerId(eventId)).thenReturn(Optional.of(organizerId));
            when(eventService.isLeader(authenticatedUser.getId(), organizerId)).thenReturn(true);
            when(eventService.getEventAnalytics(eventId)).thenReturn(analytics);
            
            ResponseEntity<?> response = eventController.getEventAnalytics(eventId, authenticatedUser);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            EventAnalyticsDTO result = (EventAnalyticsDTO) response.getBody();
            assertEquals(60L, result.getTotalTickets());
        }
        
        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401WhenNotAuthenticated() {
            ResponseEntity<?> response = eventController.getEventAnalytics(1L, null);
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should return 404 when event not found")
        void shouldReturn404WhenEventNotFound() {
            Long eventId = 999L;
            
            when(eventService.getEventOrganizerId(eventId)).thenReturn(Optional.empty());
            
            ResponseEntity<?> response = eventController.getEventAnalytics(eventId, authenticatedUser);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should return 403 when user is not org leader")
        void shouldReturn403WhenUserIsNotOrgLeader() {
            Long eventId = 1L;
            Long organizerId = 2L;
            
            when(eventService.getEventOrganizerId(eventId)).thenReturn(Optional.of(organizerId));
            when(eventService.isLeader(authenticatedUser.getId(), organizerId)).thenReturn(false);
            
            ResponseEntity<?> response = eventController.getEventAnalytics(eventId, authenticatedUser);
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }
    }
    
    @Nested
    @DisplayName("Get Tags Tests")
    class GetTagsTests {
        
        @Test
        @DisplayName("Should return all tags successfully")
        @SuppressWarnings("unchecked")
        void shouldReturnAllTags() {
            // Arrange
            List<String> tags = Arrays.asList("Tech", "Music", "Sports", "Career");
            when(eventService.getAllTags()).thenReturn(tags);
            
            // Act
            ResponseEntity<?> response = eventController.getAllTags();
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            List<String> result = (List<String>) response.getBody();
            assertEquals(4, result.size());
            assertTrue(result.contains("Tech"));
            assertTrue(result.contains("Music"));
        }
        
        @Test
        @DisplayName("Should return empty list when no tags exist")
        @SuppressWarnings("unchecked")
        void shouldReturnEmptyListWhenNoTags() {
            // Arrange
            when(eventService.getAllTags()).thenReturn(Collections.emptyList());
            
            // Act
            ResponseEntity<?> response = eventController.getAllTags();
            
            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            List<String> result = (List<String>) response.getBody();
            assertTrue(result.isEmpty());
        }
    }
}
