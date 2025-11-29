package com.example.IntegrationProjectBackend.dtos;

import jakarta.validation.constraints.NotNull;

public class SuspendUserRequest {
    @NotNull(message = "Suspended status is required")
    private Boolean suspended;

    public SuspendUserRequest() {
    }

    public SuspendUserRequest(Boolean suspended) {
        this.suspended = suspended;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }
}
