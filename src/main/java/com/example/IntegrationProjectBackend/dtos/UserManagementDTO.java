package com.example.IntegrationProjectBackend.dtos;

import com.example.IntegrationProjectBackend.models.Role;

public class UserManagementDTO {
    private Long id;
    private String cin;
    private String firstName;
    private String lastName;
    private String email;
    private int age;
    private String phoneNumber;
    private Role role;
    private boolean suspended;
    private String dtype; // For knowing if it's Student or Parent

    public UserManagementDTO() {
    }

    public UserManagementDTO(Long id, String cin, String firstName, String lastName, String email,
            int age, String phoneNumber, Role role, boolean suspended, String dtype) {
        this.id = id;
        this.cin = cin;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.suspended = suspended;
        this.dtype = dtype;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }
}
