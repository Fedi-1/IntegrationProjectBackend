package com.example.IntegrationProjectBackend.dtos;

import jakarta.validation.constraints.NotBlank;

public class SendMessageRequest {

    @NotBlank(message = "Message is required")
    private String message;

    public SendMessageRequest() {
    }

    public SendMessageRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
