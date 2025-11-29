package com.example.IntegrationProjectBackend.controllers;

import com.example.IntegrationProjectBackend.dtos.*;
import com.example.IntegrationProjectBackend.models.TicketStatus;
import com.example.IntegrationProjectBackend.services.SupportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "*")
public class SupportController {

    @Autowired
    private SupportService supportService;

    /**
     * Create a new support ticket
     * POST /api/support/tickets?userId=123
     */
    @PostMapping("/tickets")
    public ResponseEntity<AdminResponse> createTicket(
            @RequestParam Long userId,
            @Valid @RequestBody CreateTicketRequest request) {

        AdminResponse response = supportService.createTicket(userId, request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    /**
     * Get all tickets for a user
     * GET /api/support/tickets/user/{userId}
     */
    @GetMapping("/tickets/user/{userId}")
    public ResponseEntity<?> getUserTickets(@PathVariable Long userId) {
        try {
            List<SupportTicketDTO> tickets = supportService.getUserTickets(userId);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AdminResponse(false, "Error retrieving tickets: " + e.getMessage()));
        }
    }

    /**
     * Get all tickets (for admin)
     * GET /api/support/tickets
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<SupportTicketDTO>> getAllTickets() {
        List<SupportTicketDTO> tickets = supportService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get tickets by status
     * GET /api/support/tickets/status/{status}
     */
    @GetMapping("/tickets/status/{status}")
    public ResponseEntity<List<SupportTicketDTO>> getTicketsByStatus(@PathVariable TicketStatus status) {
        List<SupportTicketDTO> tickets = supportService.getTicketsByStatus(status);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get ticket by ID with messages
     * GET /api/support/tickets/{ticketId}
     */
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<?> getTicketById(@PathVariable Long ticketId) {
        Optional<SupportTicketDTO> ticket = supportService.getTicketById(ticketId);

        if (ticket.isPresent()) {
            return ResponseEntity.ok(ticket.get());
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new AdminResponse(false, "Ticket not found"));
        }
    }

    /**
     * Get messages for a ticket
     * GET /api/support/tickets/{ticketId}/messages
     */
    @GetMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<List<SupportMessageDTO>> getTicketMessages(@PathVariable Long ticketId) {
        Optional<SupportTicketDTO> ticket = supportService.getTicketById(ticketId);

        if (ticket.isPresent() && ticket.get().getMessages() != null) {
            return ResponseEntity.ok(ticket.get().getMessages());
        } else {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Send a message in a ticket
     * POST /api/support/tickets/{ticketId}/messages?userId=123
     */
    @PostMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<AdminResponse> sendMessage(
            @PathVariable Long ticketId,
            @RequestParam Long userId,
            @Valid @RequestBody SendMessageRequest request) {

        AdminResponse response = supportService.sendMessage(ticketId, userId, request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    /**
     * Update ticket status
     * PUT /api/support/tickets/{ticketId}/status
     */
    @PutMapping("/tickets/{ticketId}/status")
    public ResponseEntity<AdminResponse> updateTicketStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketStatusRequest request) {

        AdminResponse response = supportService.updateTicketStatus(ticketId, request.getStatus());

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    /**
     * Update ticket priority
     * PUT /api/support/tickets/{ticketId}/priority
     */
    @PutMapping("/tickets/{ticketId}/priority")
    public ResponseEntity<AdminResponse> updateTicketPriority(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketPriorityRequest request) {

        AdminResponse response = supportService.updateTicketPriority(ticketId, request.getPriority());

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    /**
     * Get support statistics
     * GET /api/support/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<AdminResponse> getStatistics() {
        AdminResponse response = supportService.getStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * Health check
     * GET /api/support/check
     */
    @GetMapping("/check")
    public ResponseEntity<String> checkSupport() {
        return ResponseEntity.ok("Support API is working!");
    }
}
