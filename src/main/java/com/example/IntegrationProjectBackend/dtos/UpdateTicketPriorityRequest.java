package com.example.IntegrationProjectBackend.dtos;

import com.example.IntegrationProjectBackend.models.TicketPriority;
import jakarta.validation.constraints.NotNull;

public class UpdateTicketPriorityRequest {
    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    public UpdateTicketPriorityRequest() {
    }

    public UpdateTicketPriorityRequest(TicketPriority priority) {
        this.priority = priority;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }
}
