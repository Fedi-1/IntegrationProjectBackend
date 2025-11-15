package com.example.IntegrationProjectBackend.repositories;

import com.example.IntegrationProjectBackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByCin(String cin);

    boolean existsByEmail(String email);

    boolean existsByCin(String cin);
}
