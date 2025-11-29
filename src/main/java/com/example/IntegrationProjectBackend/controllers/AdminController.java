package com.example.IntegrationProjectBackend.controllers;

import com.example.IntegrationProjectBackend.dtos.AdminResponse;
import com.example.IntegrationProjectBackend.dtos.SuspendUserRequest;
import com.example.IntegrationProjectBackend.dtos.UpdateRoleRequest;
import com.example.IntegrationProjectBackend.dtos.UserManagementDTO;
import com.example.IntegrationProjectBackend.models.Role;
import com.example.IntegrationProjectBackend.services.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Get all users with optional filters
     * GET /api/admin/users?role=ETUDIANT&suspended=true&search=john
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserManagementDTO>> getAllUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean suspended,
            @RequestParam(required = false) String search) {

        List<UserManagementDTO> users = adminService.getAllUsers(role, suspended, search);
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     * GET /api/admin/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<UserManagementDTO> user = adminService.getUserById(id);

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new AdminResponse(false, "User not found"));
        }
    }

    /**
     * Suspend or unsuspend a user
     * PUT /api/admin/users/{id}/suspend
     */
    @PutMapping("/users/{id}/suspend")
    public ResponseEntity<AdminResponse> suspendUser(
            @PathVariable Long id,
            @Valid @RequestBody SuspendUserRequest request) {

        AdminResponse response = adminService.suspendUser(id, request.getSuspended());

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    /**
     * Update user role
     * PUT /api/admin/users/{id}/role
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<AdminResponse> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {

        AdminResponse response = adminService.updateUserRole(id, request.getRole());

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    /**
     * Delete user
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<AdminResponse> deleteUser(@PathVariable Long id) {
        AdminResponse response = adminService.deleteUser(id);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    /**
     * Get statistics
     * GET /api/admin/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<AdminResponse> getStatistics() {
        AdminResponse response = adminService.getStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * Health check
     * GET /api/admin/check
     */
    @GetMapping("/check")
    public ResponseEntity<String> checkAdmin() {
        return ResponseEntity.ok("Admin API is working!");
    }
}
