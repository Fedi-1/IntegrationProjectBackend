package com.example.IntegrationProjectBackend.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class SupportMessageDTO {
    private Long id;
    private Long ticketId;
    private Long senderId;
    private String senderName;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private boolean isAdminReply;

    public SupportMessageDTO() {
    }

    public SupportMessageDTO(Long id, Long ticketId, Long senderId, String senderName,
            String message, LocalDateTime createdAt, boolean isAdminReply) {
        this.id = id;
        this.ticketId = ticketId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.createdAt = createdAt;
        this.isAdminReply = isAdminReply;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAdminReply() {
        return isAdminReply;
    }

    public void setAdminReply(boolean adminReply) {
        isAdminReply = adminReply;
    }

    // Alternative getters for frontend compatibility
    public boolean isAdminMessage() {
        return isAdminReply;
    }

    public String getContent() {
        return message;
    }
}
