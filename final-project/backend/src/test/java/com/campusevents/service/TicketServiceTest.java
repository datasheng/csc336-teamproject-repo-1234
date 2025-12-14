package com.campusevents.service;

import com.campusevents.dto.PurchaseTicketRequest;
import com.campusevents.dto.TicketConfirmationDTO;
import com.campusevents.dto.UserTicketDTO;
import com.campusevents.util.SqlExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TicketService.
 * Tests ticket purchasing and retrieval functionality with mocked dependencies.
 */
class TicketServiceTest {
    
    private SqlExecutor sqlExecutor;
    private PubSubService pubSubService;
    private TicketService ticketService;
    
    @BeforeEach
    void setUp() {
        sqlExecutor = mock(SqlExecutor.class);
        pubSubService = mock(PubSubService.class);
        ticketService = new TicketService(sqlExecutor, pubSubService);
    }
    
    @Nested
    @DisplayName("Purchase Ticket Tests")
    class PurchaseTicketTests {
        
        @Test
        @DisplayName("Should purchase ticket successfully")
        void shouldPurchaseTicketSuccessfully() {
            // Arrange
            PurchaseTicketRequest request = new PurchaseTicketRequest(1L, "General");
            
            // Event exists
            when(sqlExecutor.exists(
                eq("SELECT 1 FROM event WHERE id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(true);
            
            // Capacity is 100
            Map<String, Object> capacityRow = new HashMap<>();
            capacityRow.put("capacity", 100);
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT capacity FROM event WHERE id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(capacityRow);
            
            // 50 tickets sold
            Map<String, Object> countRow = new HashMap<>();
            countRow.put("count", 50L);
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT COUNT(*) as count FROM ticket WHERE event_id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(countRow);
            
            // Cost exists
            Map<String, Object> costRow = new HashMap<>();
            costRow.put("cost", new BigDecimal("25.00"));
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT cost FROM cost WHERE event_id = ? AND type = ?"),
                eq(new Object[]{1L, "General"})
            )).thenReturn(costRow);
            
            // User doesn't have this ticket
            when(sqlExecutor.exists(
                eq("SELECT 1 FROM ticket WHERE user_id = ? AND event_id = ? AND type = ?"),
                eq(new Object[]{10L, 1L, "General"})
            )).thenReturn(false);
            
            // Fee period
            Map<String, Object> feeRow = new HashMap<>();
            feeRow.put("id", 1);
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT id FROM fee"),
                any(Object[].class)
            )).thenReturn(feeRow);
            
            // Insert succeeds
            when(sqlExecutor.executeUpdate(
                contains("INSERT INTO ticket"),
                any(Object[].class)
            )).thenReturn(1);
            
            // Event description
            Map<String, Object> descRow = new HashMap<>();
            descRow.put("description", "Test Event");
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT description FROM event WHERE id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(descRow);
            
            // Act
            TicketConfirmationDTO result = ticketService.purchaseTicket(10L, request);
            
            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getEventId());
            assertEquals(10L, result.getUserId());
            assertEquals("General", result.getType());
            assertEquals(new BigDecimal("25.00"), result.getCost());
            assertEquals("Ticket purchased successfully", result.getMessage());
            
            // Verify Pub/Sub was called
            verify(pubSubService).publishTicketPurchased(1L, 10L, "General");
        }
        
        @Test
        @DisplayName("Should fail when event not found")
        void shouldFailWhenEventNotFound() {
            PurchaseTicketRequest request = new PurchaseTicketRequest(999L, "General");
            
            when(sqlExecutor.exists(
                eq("SELECT 1 FROM event WHERE id = ?"),
                eq(new Object[]{999L})
            )).thenReturn(false);
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.purchaseTicket(10L, request)
            );
            
            assertEquals("Event not found", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should fail when event is sold out")
        void shouldFailWhenEventSoldOut() {
            PurchaseTicketRequest request = new PurchaseTicketRequest(1L, "General");
            
            when(sqlExecutor.exists(
                eq("SELECT 1 FROM event WHERE id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(true);
            
            Map<String, Object> capacityRow = new HashMap<>();
            capacityRow.put("capacity", 100);
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT capacity FROM event WHERE id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(capacityRow);
            
            Map<String, Object> countRow = new HashMap<>();
            countRow.put("count", 100L); // Sold out
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT COUNT(*) as count FROM ticket WHERE event_id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(countRow);
            
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ticketService.purchaseTicket(10L, request)
            );
            
            assertEquals("Event is sold out", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should fail when ticket type invalid")
        void shouldFailWhenTicketTypeInvalid() {
            PurchaseTicketRequest request = new PurchaseTicketRequest(1L, "InvalidType");
            
            when(sqlExecutor.exists(
                eq("SELECT 1 FROM event WHERE id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(true);
            
            Map<String, Object> capacityRow = new HashMap<>();
            capacityRow.put("capacity", 100);
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT capacity FROM event WHERE id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(capacityRow);
            
            Map<String, Object> countRow = new HashMap<>();
            countRow.put("count", 50L);
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT COUNT(*) as count FROM ticket WHERE event_id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(countRow);
            
            // Cost not found for this type
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT cost FROM cost WHERE event_id = ? AND type = ?"),
                eq(new Object[]{1L, "InvalidType"})
            )).thenReturn(null);
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.purchaseTicket(10L, request)
            );
            
            assertEquals("Invalid ticket type for this event", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should fail when user already has ticket")
        void shouldFailWhenUserAlreadyHasTicket() {
            PurchaseTicketRequest request = new PurchaseTicketRequest(1L, "General");
            
            when(sqlExecutor.exists(
                eq("SELECT 1 FROM event WHERE id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(true);
            
            Map<String, Object> capacityRow = new HashMap<>();
            capacityRow.put("capacity", 100);
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT capacity FROM event WHERE id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(capacityRow);
            
            Map<String, Object> countRow = new HashMap<>();
            countRow.put("count", 50L);
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT COUNT(*) as count FROM ticket WHERE event_id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(countRow);
            
            Map<String, Object> costRow = new HashMap<>();
            costRow.put("cost", new BigDecimal("25.00"));
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT cost FROM cost WHERE event_id = ? AND type = ?"),
                eq(new Object[]{1L, "General"})
            )).thenReturn(costRow);
            
            // User already has ticket
            when(sqlExecutor.exists(
                eq("SELECT 1 FROM ticket WHERE user_id = ? AND event_id = ? AND type = ?"),
                eq(new Object[]{10L, 1L, "General"})
            )).thenReturn(true);
            
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ticketService.purchaseTicket(10L, request)
            );
            
            assertEquals("You already have a ticket of this type for this event", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Get User Tickets Tests")
    class GetUserTicketsTests {
        
        @Test
        @DisplayName("Should return user tickets with event details")
        void shouldReturnUserTicketsWithEventDetails() {
            List<Map<String, Object>> ticketRows = Arrays.asList(
                createTicketRow(1L, "General", "Event 1", 
                    LocalDateTime.of(2024, 3, 15, 10, 0),
                    LocalDateTime.of(2024, 3, 15, 18, 0),
                    "Org 1", new BigDecimal("25.00")),
                createTicketRow(2L, "VIP", "Event 2",
                    LocalDateTime.of(2024, 4, 1, 14, 0),
                    LocalDateTime.of(2024, 4, 1, 20, 0),
                    "Org 2", new BigDecimal("75.00"))
            );
            
            when(sqlExecutor.executeQuery(
                contains("FROM ticket t"),
                eq(new Object[]{10L})
            )).thenReturn(ticketRows);
            
            List<UserTicketDTO> result = ticketService.getUserTickets(10L);
            
            assertEquals(2, result.size());
            assertEquals(1L, result.get(0).getEventId());
            assertEquals("General", result.get(0).getType());
            assertEquals("Event 1", result.get(0).getEventDescription());
            assertEquals(new BigDecimal("25.00"), result.get(0).getCost());
            
            assertEquals(2L, result.get(1).getEventId());
            assertEquals("VIP", result.get(1).getType());
        }
        
        @Test
        @DisplayName("Should return empty list when user has no tickets")
        void shouldReturnEmptyListWhenNoTickets() {
            when(sqlExecutor.executeQuery(
                contains("FROM ticket t"),
                eq(new Object[]{10L})
            )).thenReturn(Collections.emptyList());
            
            List<UserTicketDTO> result = ticketService.getUserTickets(10L);
            
            assertTrue(result.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Pub/Sub Integration Tests")
    class PubSubIntegrationTests {
        
        @Test
        @DisplayName("Should publish ticket purchased message after successful purchase")
        void shouldPublishTicketPurchasedAfterPurchase() {
            // Arrange
            PurchaseTicketRequest request = new PurchaseTicketRequest(1L, "VIP");
            
            setupSuccessfulPurchaseMocks(1L, "VIP", 100, 10, new BigDecimal("50.00"));
            
            // Act
            ticketService.purchaseTicket(5L, request);
            
            // Assert - verify Pub/Sub was called with correct parameters
            verify(pubSubService, times(1)).publishTicketPurchased(1L, 5L, "VIP");
        }
        
        @Test
        @DisplayName("Should not publish when purchase fails due to sold out")
        void shouldNotPublishWhenSoldOut() {
            PurchaseTicketRequest request = new PurchaseTicketRequest(1L, "General");
            
            when(sqlExecutor.exists(
                eq("SELECT 1 FROM event WHERE id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(true);
            
            Map<String, Object> capacityRow = new HashMap<>();
            capacityRow.put("capacity", 100);
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT capacity FROM event WHERE id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(capacityRow);
            
            Map<String, Object> countRow = new HashMap<>();
            countRow.put("count", 100L); // Sold out
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT COUNT(*) as count FROM ticket WHERE event_id = ?"),
                eq(new Object[]{1L})
            )).thenReturn(countRow);
            
            assertThrows(IllegalStateException.class, () -> ticketService.purchaseTicket(5L, request));
            
            // Verify Pub/Sub was never called
            verify(pubSubService, never()).publishTicketPurchased(anyLong(), anyLong(), anyString());
        }
        
        @Test
        @DisplayName("Should not publish when event not found")
        void shouldNotPublishWhenEventNotFound() {
            PurchaseTicketRequest request = new PurchaseTicketRequest(999L, "General");
            
            when(sqlExecutor.exists(
                eq("SELECT 1 FROM event WHERE id = ?"),
                eq(new Object[]{999L})
            )).thenReturn(false);
            
            assertThrows(IllegalArgumentException.class, () -> ticketService.purchaseTicket(5L, request));
            
            verify(pubSubService, never()).publishTicketPurchased(anyLong(), anyLong(), anyString());
        }
        
        private void setupSuccessfulPurchaseMocks(Long eventId, String type, int capacity, int ticketsSold, BigDecimal cost) {
            when(sqlExecutor.exists(
                eq("SELECT 1 FROM event WHERE id = ?"),
                eq(new Object[]{eventId})
            )).thenReturn(true);
            
            Map<String, Object> capacityRow = new HashMap<>();
            capacityRow.put("capacity", capacity);
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT capacity FROM event WHERE id = ?"),
                eq(new Object[]{eventId})
            )).thenReturn(capacityRow);
            
            Map<String, Object> countRow = new HashMap<>();
            countRow.put("count", (long) ticketsSold);
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT COUNT(*) as count FROM ticket WHERE event_id = ?"),
                eq(new Object[]{eventId})
            )).thenReturn(countRow);
            
            Map<String, Object> costRow = new HashMap<>();
            costRow.put("cost", cost);
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT cost FROM cost WHERE event_id = ? AND type = ?"),
                eq(new Object[]{eventId, type})
            )).thenReturn(costRow);
            
            when(sqlExecutor.exists(
                eq("SELECT 1 FROM ticket WHERE user_id = ? AND event_id = ? AND type = ?"),
                any(Object[].class)
            )).thenReturn(false);
            
            Map<String, Object> feeRow = new HashMap<>();
            feeRow.put("id", 1);
            when(sqlExecutor.executeQueryForMap(
                contains("SELECT id FROM fee"),
                any(Object[].class)
            )).thenReturn(feeRow);
            
            when(sqlExecutor.executeUpdate(
                contains("INSERT INTO ticket"),
                any(Object[].class)
            )).thenReturn(1);
            
            Map<String, Object> descRow = new HashMap<>();
            descRow.put("description", "Test Event");
            when(sqlExecutor.executeQueryForMap(
                eq("SELECT description FROM event WHERE id = ?"),
                eq(new Object[]{eventId})
            )).thenReturn(descRow);
        }
    }
    
    // Helper methods
    private Map<String, Object> createTicketRow(Long eventId, String type, String description,
            LocalDateTime startTime, LocalDateTime endTime, String organizerName, BigDecimal cost) {
        Map<String, Object> row = new HashMap<>();
        row.put("event_id", eventId);
        row.put("type", type);
        row.put("description", description);
        row.put("start_time", Timestamp.valueOf(startTime));
        row.put("end_time", Timestamp.valueOf(endTime));
        row.put("organizer_name", organizerName);
        row.put("cost", cost);
        return row;
    }
}
