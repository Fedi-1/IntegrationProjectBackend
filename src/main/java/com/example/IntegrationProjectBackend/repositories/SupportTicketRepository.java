package com.example.IntegrationProjectBackend.repositories;

import com.example.IntegrationProjectBackend.models.SupportTicket;
import com.example.IntegrationProjectBackend.models.TicketStatus;
import com.example.IntegrationProjectBackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    // Find all tickets by user
    List<SupportTicket> findByCreatedByOrderByUpdatedAtDesc(User user);

    // Find tickets by status
    List<SupportTicket> findByStatusOrderByUpdatedAtDesc(TicketStatus status);

    // Find all tickets ordered by updated date
    List<SupportTicket> findAllByOrderByUpdatedAtDesc();

    // Count open tickets for a user
    long countByCreatedByAndStatus(User user, TicketStatus status);

    // Count all open tickets
    long countByStatus(TicketStatus status);

    // Search tickets
    @Query("SELECT t FROM SupportTicket t WHERE " +
           "LOWER(t.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<SupportTicket> searchTickets(@Param("search") String search);
}
