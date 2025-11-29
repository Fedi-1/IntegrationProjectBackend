package com.example.IntegrationProjectBackend.services;

import com.example.IntegrationProjectBackend.dtos.*;
import com.example.IntegrationProjectBackend.models.*;
import com.example.IntegrationProjectBackend.repositories.SupportMessageRepository;
import com.example.IntegrationProjectBackend.repositories.SupportTicketRepository;
import com.example.IntegrationProjectBackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SupportService {

    @Autowired
    private SupportTicketRepository ticketRepository;

    @Autowired
    private SupportMessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new support ticket
     */
    @Transactional
    public AdminResponse createTicket(Long userId, CreateTicketRequest request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            SupportTicket ticket = new SupportTicket(
                    request.getSubject(),
                    request.getDescription(),
                    user,
                    request.getPriority());

            ticket = ticketRepository.save(ticket);

            return new AdminResponse(true, "Ticket created successfully", convertToDTO(ticket, false));

        } catch (Exception e) {
            return new AdminResponse(false, "Failed to create ticket: " + e.getMessage());
        }
    }

    /**
     * Get all tickets for a user
     */
    public List<SupportTicketDTO> getUserTickets(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SupportTicket> tickets = ticketRepository.findByCreatedByOrderByUpdatedAtDesc(user);
        return tickets.stream()
                .map(ticket -> convertToDTO(ticket, false))
                .collect(Collectors.toList());
    }

    /**
     * Get all tickets (for admin)
     */
    public List<SupportTicketDTO> getAllTickets() {
        List<SupportTicket> tickets = ticketRepository.findAllByOrderByUpdatedAtDesc();
        return tickets.stream()
                .map(ticket -> convertToDTO(ticket, false))
                .collect(Collectors.toList());
    }

    /**
     * Get tickets by status
     */
    public List<SupportTicketDTO> getTicketsByStatus(TicketStatus status) {
        List<SupportTicket> tickets = ticketRepository.findByStatusOrderByUpdatedAtDesc(status);
        return tickets.stream()
                .map(ticket -> convertToDTO(ticket, false))
                .collect(Collectors.toList());
    }

    /**
     * Get ticket by ID with messages
     */
    public Optional<SupportTicketDTO> getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> convertToDTO(ticket, true));
    }

    /**
     * Send a message in a ticket
     */
    @Transactional
    public AdminResponse sendMessage(Long ticketId, Long userId, SendMessageRequest request) {
        try {
            SupportTicket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            User sender = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user is authorized (creator or admin)
            if (!ticket.getCreatedBy().getId().equals(userId) && sender.getRole() != Role.ADMINISTRATOR) {
                return new AdminResponse(false, "Unauthorized to send message in this ticket");
            }

            boolean isAdminReply = sender.getRole() == Role.ADMINISTRATOR;

            // If admin is replying and ticket is OPEN, change to IN_PROGRESS
            if (isAdminReply && ticket.getStatus() == TicketStatus.OPEN) {
                ticket.setStatus(TicketStatus.IN_PROGRESS);
            }

            SupportMessage message = new SupportMessage(ticket, sender, request.getMessage(), isAdminReply);
            message = messageRepository.save(message);

            ticket.addMessage(message);
            ticketRepository.save(ticket);

            return new AdminResponse(true, "Message sent successfully", convertMessageToDTO(message));

        } catch (Exception e) {
            return new AdminResponse(false, "Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Update ticket status
     */
    @Transactional
    public AdminResponse updateTicketStatus(Long ticketId, TicketStatus status) {
        try {
            SupportTicket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            ticket.setStatus(status);
            ticketRepository.save(ticket);

            return new AdminResponse(true, "Ticket status updated successfully", convertToDTO(ticket, false));

        } catch (Exception e) {
            return new AdminResponse(false, "Failed to update ticket status: " + e.getMessage());
        }
    }

    /**
     * Update ticket priority
     */
    @Transactional
    public AdminResponse updateTicketPriority(Long ticketId, TicketPriority priority) {
        try {
            SupportTicket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            ticket.setPriority(priority);
            ticketRepository.save(ticket);

            return new AdminResponse(true, "Ticket priority updated successfully", convertToDTO(ticket, false));

        } catch (Exception e) {
            return new AdminResponse(false, "Failed to update ticket priority: " + e.getMessage());
        }
    }

    /**
     * Get ticket statistics
     */
    public AdminResponse getStatistics() {
        long totalTickets = ticketRepository.count();
        long openTickets = ticketRepository.countByStatus(TicketStatus.OPEN);
        long inProgressTickets = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long resolvedTickets = ticketRepository.countByStatus(TicketStatus.RESOLVED);
        long closedTickets = ticketRepository.countByStatus(TicketStatus.CLOSED);

        var stats = new java.util.HashMap<String, Long>();
        stats.put("totalTickets", totalTickets);
        stats.put("openTickets", openTickets);
        stats.put("inProgressTickets", inProgressTickets);
        stats.put("resolvedTickets", resolvedTickets);
        stats.put("closedTickets", closedTickets);

        return new AdminResponse(true, "Statistics retrieved successfully", stats);
    }

    /**
     * Convert SupportTicket to DTO
     */
    private SupportTicketDTO convertToDTO(SupportTicket ticket, boolean includeMessages) {
        SupportTicketDTO dto = new SupportTicketDTO();
        dto.setId(ticket.getId());
        dto.setSubject(ticket.getSubject());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus());
        dto.setPriority(ticket.getPriority());
        dto.setCreatedById(ticket.getCreatedBy().getId());
        String fullName = ticket.getCreatedBy().getFirstName() + " " + ticket.getCreatedBy().getLastName();
        dto.setCreatedByName(fullName);
        dto.setUserName(fullName); // For admin view
        dto.setCreatedByEmail(ticket.getCreatedBy().getEmail());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setMessageCount(ticket.getMessages().size());

        if (includeMessages) {
            List<SupportMessageDTO> messageDTOs = ticket.getMessages().stream()
                    .map(this::convertMessageToDTO)
                    .collect(Collectors.toList());
            dto.setMessages(messageDTOs);
        }

        return dto;
    }

    /**
     * Convert SupportMessage to DTO
     */
    private SupportMessageDTO convertMessageToDTO(SupportMessage message) {
        return new SupportMessageDTO(
                message.getId(),
                message.getTicket().getId(),
                message.getSender().getId(),
                message.getSender().getFirstName() + " " + message.getSender().getLastName(),
                message.getMessage(),
                message.getCreatedAt(),
                message.isAdminReply());
    }
}
