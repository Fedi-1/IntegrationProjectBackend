package com.example.IntegrationProjectBackend.services;

import com.example.IntegrationProjectBackend.dtos.AdminResponse;
import com.example.IntegrationProjectBackend.dtos.UserManagementDTO;
import com.example.IntegrationProjectBackend.models.Role;
import com.example.IntegrationProjectBackend.models.Student;
import com.example.IntegrationProjectBackend.models.User;
import com.example.IntegrationProjectBackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all users with optional filters
     */
    public List<UserManagementDTO> getAllUsers(Role role, Boolean suspended, String search) {
        List<User> users;

        if (search == null) {
            search = "";
        }

        if (role != null || suspended != null || !search.isEmpty()) {
            users = userRepository.findUsersWithFilters(role, suspended, search);
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    public Optional<UserManagementDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Suspend or unsuspend a user
     */
    @Transactional
    public AdminResponse suspendUser(Long userId, boolean suspended) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return new AdminResponse(false, "User not found");
        }

        User user = userOptional.get();

        // Prevent suspending administrators
        if (user.getRole() == Role.ADMINISTRATOR) {
            return new AdminResponse(false, "Cannot suspend administrator accounts");
        }

        user.setSuspended(suspended);
        userRepository.save(user);

        String action = suspended ? "suspended" : "unsuspended";
        return new AdminResponse(true, "User " + action + " successfully", convertToDTO(user));
    }

    /**
     * Update user role
     */
    @Transactional
    public AdminResponse updateUserRole(Long userId, Role newRole) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return new AdminResponse(false, "User not found");
        }

        User user = userOptional.get();
        Role oldRole = user.getRole();

        // Prevent changing role of the last administrator
        if (oldRole == Role.ADMINISTRATOR && newRole != Role.ADMINISTRATOR) {
            long adminCount = userRepository.findByRole(Role.ADMINISTRATOR).size();
            if (adminCount <= 1) {
                return new AdminResponse(false, "Cannot change role of the last administrator");
            }
        }

        user.setRole(newRole);
        userRepository.save(user);

        return new AdminResponse(true, "User role updated successfully", convertToDTO(user));
    }

    /**
     * Delete user
     */
    @Transactional
    public AdminResponse deleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return new AdminResponse(false, "User not found");
        }

        User user = userOptional.get();

        // Prevent deleting administrators
        if (user.getRole() == Role.ADMINISTRATOR) {
            long adminCount = userRepository.findByRole(Role.ADMINISTRATOR).size();
            if (adminCount <= 1) {
                return new AdminResponse(false, "Cannot delete the last administrator");
            }
        }

        userRepository.delete(user);

        return new AdminResponse(true, "User deleted successfully");
    }

    /**
     * Get statistics
     */
    public AdminResponse getStatistics() {
        long totalUsers = userRepository.count();
        long totalStudents = userRepository.findByRole(Role.ETUDIANT).size();
        long totalParents = userRepository.findByRole(Role.PARENT).size();
        long totalAdmins = userRepository.findByRole(Role.ADMINISTRATOR).size();
        long suspendedUsers = userRepository.findBySuspended(true).size();

        var stats = new java.util.HashMap<String, Long>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalStudents", totalStudents);
        stats.put("totalParents", totalParents);
        stats.put("totalAdmins", totalAdmins);
        stats.put("suspendedUsers", suspendedUsers);

        return new AdminResponse(true, "Statistics retrieved successfully", stats);
    }

    /**
     * Convert User entity to UserManagementDTO
     */
    private UserManagementDTO convertToDTO(User user) {
        String dtype = "User";
        if (user instanceof Student) {
            dtype = "Student";
        } else if (user.getRole() == Role.PARENT) {
            dtype = "Parent";
        }

        return new UserManagementDTO(
                user.getId(),
                user.getCin(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getAge(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isSuspended(),
                dtype);
    }
}
