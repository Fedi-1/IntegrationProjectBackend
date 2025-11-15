package com.example.IntegrationProjectBackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private boolean success;
    private String message;
    private UserDTO user;

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
