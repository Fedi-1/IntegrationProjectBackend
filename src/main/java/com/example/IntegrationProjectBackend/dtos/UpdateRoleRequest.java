package com.example.IntegrationProjectBackend.dtos;

import com.example.IntegrationProjectBackend.models.Role;
import jakarta.validation.constraints.NotNull;

public class UpdateRoleRequest {
    @NotNull(message = "Role is required")
    private Role role;

    public UpdateRoleRequest() {
    }

    public UpdateRoleRequest(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
