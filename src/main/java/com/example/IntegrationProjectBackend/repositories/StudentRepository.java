package com.example.IntegrationProjectBackend.repositories;

import com.example.IntegrationProjectBackend.models.Parent;
import com.example.IntegrationProjectBackend.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    Optional<Student> findByCin(String cin);

    Optional<Student> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Student> findByParent(Parent parent);
}
