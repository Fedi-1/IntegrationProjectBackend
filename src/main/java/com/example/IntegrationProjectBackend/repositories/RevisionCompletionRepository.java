package com.example.IntegrationProjectBackend.repositories;

import com.example.IntegrationProjectBackend.models.RevisionCompletion;
import com.example.IntegrationProjectBackend.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RevisionCompletionRepository extends JpaRepository<RevisionCompletion, Long> {
    List<RevisionCompletion> findByStudent(Student student);

    List<RevisionCompletion> findByStudentAndSubject(Student student, String subject);
}
