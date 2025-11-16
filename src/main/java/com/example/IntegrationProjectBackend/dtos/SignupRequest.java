package com.example.IntegrationProjectBackend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "CIN is required")
    @Size(min = 5, max = 20, message = "CIN must be between 5 and 20 characters")
    private String cin;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotNull(message = "Age is required")
    private Integer age;

    @NotBlank(message = "Role is required")
    private String role; // ETUDIANT, PARENT, ADMINISTRATOR

    // For students only
    private String parentCin;
    private Integer maxStudyDuration;
    private Integer preparationTimeMinutes;
}
