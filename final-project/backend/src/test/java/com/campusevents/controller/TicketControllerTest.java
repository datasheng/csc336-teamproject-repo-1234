package com.campusevents.controller;

import com.campusevents.dto.PurchaseTicketRequest;
import com.campusevents.dto.TicketConfirmationDTO;
import com.campusevents.model.User;
import com.campusevents.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TicketController.
 * Tests REST endpoint handling with mocked TicketService.
 */
class TicketControllerTest {
    
    private TicketService ticketService;
    private TicketController ticketController;
    private User authenticatedUser;
    
    @BeforeEach
    void setUp() {
        ticketService = mock(TicketService.class);
        ticketController = new TicketController(ticketService);
        
        // Create a test user
        authenticatedUser = new User(1L, "John", "Doe", "john@example.com", "hashedPassword", 1L);
    }
    
    @Nested
    @DisplayName("Purchase Ticket Tests")
    class PurchaseTicketTests {
        
        @Test
        @DisplayName("Should purchase ticket successfully")
        void shouldPurchaseTicketSuccessfully() {
            PurchaseTicketRequest request = new PurchaseTicketRequest(1L, "General");
            
            TicketConfirmationDTO confirmation = new TicketConfirmationDTO();
            confirmation.setEventId(1L);
            confirmation.setUserId(1L);
            confirmation.setType("General");
            confirmation.setCost(new BigDecimal("25.00"));
            confirmation.setMessage("Ticket purchased successfully");
            
            when(ticketService.purchaseTicket(1L, request)).thenReturn(confirmation);
            
            ResponseEntity<?> response = ticketController.purchaseTicket(authenticatedUser, request);
            
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof TicketConfirmationDTO);
            assertEquals("General", ((TicketConfirmationDTO) response.getBody()).getType());
        }
        
        @Test
        @DisplayName("Should reject when eventId is missing")
        void shouldRejectMissingEventId() {
            PurchaseTicketRequest request = new PurchaseTicketRequest(null, "General");
            
            ResponseEntity<?> response = ticketController.purchaseTicket(authenticatedUser, request);
            
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should reject when type is missing")
        void shouldRejectMissingType() {
            PurchaseTicketRequest request = new PurchaseTicketRequest(1L, null);
            
            ResponseEntity<?> response = ticketController.purchaseTicket(authenticatedUser, request);
            
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should reject when type is empty")
        void shouldRejectEmptyType() {
            PurchaseTicketRequest request = new PurchaseTicketRequest(1L, "  ");
            
            ResponseEntity<?> response = ticketController.purchaseTicket(authenticatedUser, request);
            
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should return 404 when event not found")
        void shouldReturn404WhenEventNotFound() {
            PurchaseTicketRequest request = new PurchaseTicketRequest(999L, "General");
            
            when(ticketService.purchaseTicket(1L, request))
                .thenThrow(new IllegalArgumentException("Event not found"));
            
            ResponseEntity<?> response = ticketController.purchaseTicket(authenticatedUser, request);
            
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should return 400 when ticket type invalid")
        void shouldReturn400WhenTicketTypeInvalid() {
            PurchaseTicketRequest request = new PurchaseTicketRequest(1L, "InvalidType");
            
            when(ticketService.purchaseTicket(1L, request))
                .thenThrow(new IllegalArgumentException("Invalid ticket type for this event"));
            
            ResponseEntity<?> response = ticketController.purchaseTicket(authenticatedUser, request);
            
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should return 409 when event sold out")
        void shouldReturn409WhenEventSoldOut() {
            PurchaseTicketRequest request = new PurchaseTicketRequest(1L, "General");
            
            when(ticketService.purchaseTicket(1L, request))
                .thenThrow(new IllegalStateException("Event is sold out"));
            
            ResponseEntity<?> response = ticketController.purchaseTicket(authenticatedUser, request);
            
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should return 409 when user already has ticket")
        void shouldReturn409WhenUserAlreadyHasTicket() {
            PurchaseTicketRequest request = new PurchaseTicketRequest(1L, "General");
            
            when(ticketService.purchaseTicket(1L, request))
                .thenThrow(new IllegalStateException("You already have a ticket of this type for this event"));
            
            ResponseEntity<?> response = ticketController.purchaseTicket(authenticatedUser, request);
            
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }
    }
}
