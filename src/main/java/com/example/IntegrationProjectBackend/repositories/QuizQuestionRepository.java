package com.example.IntegrationProjectBackend.repositories;

import com.example.IntegrationProjectBackend.models.Quiz;
import com.example.IntegrationProjectBackend.models.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizOrderByQuestionOrderAsc(Quiz quiz);
}
