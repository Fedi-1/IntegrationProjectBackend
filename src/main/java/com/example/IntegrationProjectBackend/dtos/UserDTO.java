package com.example.IntegrationProjectBackend.dtos;

import com.example.IntegrationProjectBackend.models.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String cin;
    private String phoneNumber;
    private Integer age;
    private Role role;

    private String parentCin;
    private Integer maxStudyDuration;
}
