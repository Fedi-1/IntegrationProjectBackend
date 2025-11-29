package com.example.IntegrationProjectBackend.repositories;

import com.example.IntegrationProjectBackend.models.Role;
import com.example.IntegrationProjectBackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByCin(String cin);

    boolean existsByEmail(String email);

    boolean existsByCin(String cin);

    // Admin management queries
    List<User> findByRole(Role role);

    List<User> findBySuspended(boolean suspended);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.cin) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);

    @Query("SELECT u FROM User u WHERE " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:suspended IS NULL OR u.suspended = :suspended) AND " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.cin) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> findUsersWithFilters(@Param("role") Role role,
            @Param("suspended") Boolean suspended,
            @Param("search") String search);
}
