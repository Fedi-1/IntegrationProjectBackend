package com.example.IntegrationProjectBackend.repositories;

import com.example.IntegrationProjectBackend.models.Quiz;
import com.example.IntegrationProjectBackend.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByStudent(Student student);

    List<Quiz> findByStudentAndStatus(Student student, String status);

    List<Quiz> findByStudentAndStatusOrderByCompletedAtDesc(Student student, String status);

    List<Quiz> findByStudentAndSubject(Student student, String subject);

    List<Quiz> findByStatusAndCompletedAtAfter(String status, LocalDateTime completedAt);
}
