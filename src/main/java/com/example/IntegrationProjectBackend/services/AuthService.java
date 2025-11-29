package com.example.IntegrationProjectBackend.services;

import com.example.IntegrationProjectBackend.dtos.*;
import com.example.IntegrationProjectBackend.models.Parent;
import com.example.IntegrationProjectBackend.models.Role;
import com.example.IntegrationProjectBackend.models.Student;
import com.example.IntegrationProjectBackend.models.User;
import com.example.IntegrationProjectBackend.repositories.ParentRepository;
import com.example.IntegrationProjectBackend.repositories.StudentRepository;
import com.example.IntegrationProjectBackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                return new AuthResponse(false, "Email already exists");
            }

            // Check if CIN already exists
            if (userRepository.existsByCin(request.getCin())) {
                return new AuthResponse(false, "CIN already exists");
            }

            // Validate role
            Role role;
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                return new AuthResponse(false, "Invalid role. Must be ETUDIANT or PARENT");
            }

            // Prevent admin creation through public signup
            if (role == Role.ADMINISTRATOR) {
                return new AuthResponse(false, "Administrator accounts cannot be created through public signup");
            }

            // Create user based on role
            User user;
            if (role == Role.ETUDIANT) {
                // Validate parent CIN if provided
                if (request.getParentCin() != null && !request.getParentCin().isEmpty()) {
                    Optional<Parent> parentOpt = parentRepository.findByCin(request.getParentCin());
                    if (parentOpt.isEmpty()) {
                        return new AuthResponse(false, "Parent with CIN " + request.getParentCin()
                                + " not found. Please ask your parent to create an account first.");
                    }
                }

                Student student = new Student();
                student.setFirstName(request.getFirstName());
                student.setLastName(request.getLastName());
                student.setEmail(request.getEmail());
                student.setPassword(request.getPassword()); // In production, hash this!
                student.setCin(request.getCin());
                student.setPhoneNumber(request.getPhoneNumber());
                student.setAge(request.getAge());
                student.setRole(role);
                student.setParentCin(request.getParentCin() != null ? request.getParentCin() : "");
                student.setMaxStudyDuration(
                        request.getMaxStudyDuration() != null ? request.getMaxStudyDuration() : 240); // Default 240
                                                                                                      // minutes (4
                                                                                                      // hours)
                student.setPreparationTimeMinutes(
                        request.getPreparationTimeMinutes() != null ? request.getPreparationTimeMinutes() : 30); // Default
                                                                                                                 // 30
                                                                                                                 // minutes

                // Link to parent if parentCin is provided
                if (request.getParentCin() != null && !request.getParentCin().isEmpty()) {
                    parentRepository.findByCin(request.getParentCin()).ifPresent(student::setParent);
                }

                user = studentRepository.save(student);
            } else if (role == Role.PARENT) {
                // Create Parent entity
                Parent parent = new Parent();
                parent.setFirstName(request.getFirstName());
                parent.setLastName(request.getLastName());
                parent.setEmail(request.getEmail());
                parent.setPassword(request.getPassword()); // In production, hash this!
                parent.setCin(request.getCin());
                parent.setPhoneNumber(request.getPhoneNumber());
                parent.setAge(request.getAge());
                parent.setRole(role);

                user = parentRepository.save(parent);
            } else {
                User newUser = new User();
                newUser.setFirstName(request.getFirstName());
                newUser.setLastName(request.getLastName());
                newUser.setEmail(request.getEmail());
                newUser.setPassword(request.getPassword()); // In production, hash this!
                newUser.setCin(request.getCin());
                newUser.setPhoneNumber(request.getPhoneNumber());
                newUser.setAge(request.getAge());
                newUser.setRole(role);

                user = userRepository.save(newUser);
            }

            // Convert to DTO
            UserDTO userDTO = convertToDTO(user);

            return new AuthResponse(true, "User registered successfully", userDTO);

        } catch (Exception e) {
            return new AuthResponse(false, "Registration failed: " + e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest request) {
        try {
            // Find user by email
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Invalid email or password"));

            // Check password (In production, use BCrypt!)
            if (!user.getPassword().equals(request.getPassword())) {
                return new AuthResponse(false, "Invalid email or password");
            }

            // Check if account is suspended
            if (user.isSuspended()) {
                return new AuthResponse(false, "Your account has been suspended. Please contact the administrator.");
            }

            // Convert to DTO
            UserDTO userDTO = convertToDTO(user);

            return new AuthResponse(true, "Login successful", userDTO);

        } catch (Exception e) {
            return new AuthResponse(false, "Login failed: " + e.getMessage());
        }
    }

    public AuthResponse changePassword(ChangePasswordRequest request) {
        try {
            // Find user by CIN
            User user = userRepository.findByCin(request.getCin())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Trim passwords and verify current password
            String storedPassword = user.getPassword() != null ? user.getPassword().trim() : "";
            String providedPassword = request.getCurrentPassword() != null ? request.getCurrentPassword().trim() : "";

            System.out.println("DEBUG - CIN: " + request.getCin());
            System.out.println("DEBUG - Stored password length: " + storedPassword.length());
            System.out.println("DEBUG - Provided password length: " + providedPassword.length());
            System.out.println("DEBUG - Passwords match: " + storedPassword.equals(providedPassword));

            if (!storedPassword.equals(providedPassword)) {
                return new AuthResponse(false, "Current password is incorrect");
            }

            // Update to new password
            user.setPassword(request.getNewPassword().trim()); // In production, hash this!
            userRepository.save(user);

            return new AuthResponse(true, "Password changed successfully");

        } catch (RuntimeException e) {
            return new AuthResponse(false, e.getMessage());
        } catch (Exception e) {
            return new AuthResponse(false, "Failed to change password: " + e.getMessage());
        }
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setCin(user.getCin());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAge(user.getAge());
        dto.setRole(user.getRole());

        if (user instanceof Student) {
            Student student = (Student) user;
            dto.setParentCin(student.getParentCin());
            dto.setMaxStudyDuration(student.getMaxStudyDuration());
        }

        return dto;
    }
}
