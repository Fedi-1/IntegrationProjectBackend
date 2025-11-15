package com.example.IntegrationProjectBackend.repositories;

import com.example.IntegrationProjectBackend.models.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {
    Optional<Parent> findByCin(String cin);

    Optional<Parent> findByEmail(String email);
}
