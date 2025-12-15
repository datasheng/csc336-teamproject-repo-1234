package com.campusevents.controller;

import com.campusevents.dto.ErrorResponse;
import com.campusevents.dto.PurchaseTicketRequest;
import com.campusevents.dto.TicketConfirmationDTO;
import com.campusevents.model.User;
import com.campusevents.security.CurrentUser;
import com.campusevents.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for ticket management endpoints.
 * 
 * Endpoints:
 * - POST /api/tickets - Purchase a ticket (authenticated)
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    
    private final TicketService ticketService;
    
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }
    
    /**
     * Purchase a ticket for an event.
     * 
     * Request body:
     * {
     *   "eventId": 1,
     *   "type": "General"
     * }
     * 
     * @param user The authenticated user (injected by @CurrentUser)
     * @param request The purchase request
     * @return Ticket confirmation with cost
     */
    @PostMapping
    public ResponseEntity<?> purchaseTicket(
            @CurrentUser User user,
            @RequestBody PurchaseTicketRequest request) {
        try {
            // Validate required fields
            if (request.getEventId() == null) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "Event ID is required", 400)
                );
            }
            if (request.getType() == null || request.getType().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Bad Request", "Ticket type is required", 400)
                );
            }
            
            // Purchase the ticket
            TicketConfirmationDTO confirmation = ticketService.purchaseTicket(user.getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(confirmation);
            
        } catch (IllegalArgumentException e) {
            // Event not found or invalid ticket type
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Not Found", e.getMessage(), 404)
                );
            }
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Bad Request", e.getMessage(), 400)
            );
        } catch (IllegalStateException e) {
            // Capacity exceeded or already purchased
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse("Conflict", e.getMessage(), 409)
            );
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ticket purchase error: " + e.getClass().getName() + " - " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Internal Server Error", "An error occurred while purchasing the ticket", 500)
            );
        }
    }
}
