package com.example.IntegrationProjectBackend.repositories;

import com.example.IntegrationProjectBackend.models.Subjects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectsRepository extends JpaRepository<Subjects, Long> {

    Optional<Subjects> findByName(String name);

    boolean existsByName(String name);
}
