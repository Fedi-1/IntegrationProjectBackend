package com.example.IntegrationProjectBackend.repositories;

import com.example.IntegrationProjectBackend.models.SupportMessage;
import com.example.IntegrationProjectBackend.models.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    // Find all messages for a ticket
    List<SupportMessage> findByTicketOrderByCreatedAtAsc(SupportTicket ticket);

    // Find all messages for a ticket by ID
    List<SupportMessage> findByTicket_IdOrderByCreatedAtAsc(Long ticketId);

    // Count messages in a ticket
    long countByTicket(SupportTicket ticket);
}
