package com.example.IntegrationProjectBackend.controllers;

import com.example.IntegrationProjectBackend.dtos.AuthResponse;
import com.example.IntegrationProjectBackend.dtos.ChangePasswordRequest;
import com.example.IntegrationProjectBackend.dtos.LoginRequest;
import com.example.IntegrationProjectBackend.dtos.SignupRequest;
import com.example.IntegrationProjectBackend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody SignupRequest request,
            BindingResult bindingResult) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            var fieldError = bindingResult.getFieldError();
            String errorMessage = fieldError != null
                    ? fieldError.getDefaultMessage()
                    : "Validation error";
            return ResponseEntity
                    .badRequest()
                    .body(new AuthResponse(false, errorMessage));
        }

        AuthResponse response = authService.signup(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            BindingResult bindingResult) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            var fieldError = bindingResult.getFieldError();
            String errorMessage = fieldError != null
                    ? fieldError.getDefaultMessage()
                    : "Validation error";
            return ResponseEntity
                    .badRequest()
                    .body(new AuthResponse(false, errorMessage));
        }

        AuthResponse response = authService.login(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<String> checkAuth() {
        return ResponseEntity.ok("Auth API is working!");
    }

    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(
            @RequestBody ChangePasswordRequest request) {

        // Validate input
        if (request.getCin() == null || request.getCin().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new AuthResponse(false, "CIN is required"));
        }

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new AuthResponse(false, "Current password is required"));
        }

        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new AuthResponse(false, "New password is required"));
        }

        if (request.getNewPassword().length() < 6) {
            return ResponseEntity
                    .badRequest()
                    .body(new AuthResponse(false, "New password must be at least 6 characters"));
        }

        AuthResponse response = authService.changePassword(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }
}
