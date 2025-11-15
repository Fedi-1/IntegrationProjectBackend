package com.example.IntegrationProjectBackend.controllers;

import com.example.IntegrationProjectBackend.models.*;
import com.example.IntegrationProjectBackend.repositories.*;
import com.example.IntegrationProjectBackend.services.AIQuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "*")
public class QuizController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RevisionCompletionRepository revisionCompletionRepository;

    @Autowired
    private SubjectsRepository subjectsRepository;

    @Autowired
    private AIQuizService aiQuizService; // Spring AI for Groq

    @GetMapping("/subjects/{studentCin}")
    public ResponseEntity<?> getStudentSubjects(@PathVariable String studentCin) {
        Optional<Student> studentOpt = studentRepository.findByCin(studentCin);
        if (!studentOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
        }
        Student student = studentOpt.get();

        // Get all subjects for this student from their schedules
        List<Subjects> subjects = subjectsRepository.findAll();

        // Return list of subject names
        List<String> subjectNames = subjects.stream()
                .map(Subjects::getName)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(Map.of("success", true, "subjects", subjectNames));
    }

    @PostMapping("/generate/{studentCin}")
    public ResponseEntity<?> generateQuiz(
            @PathVariable String studentCin,
            @RequestBody Map<String, Object> request) {

        Optional<Student> studentOpt = studentRepository.findByCin(studentCin);
        if (!studentOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
        }
        Student student = studentOpt.get();

        String subject = (String) request.get("subject");
        String topic = (String) request.get("topic");
        String difficulty = request.containsKey("difficulty") ? (String) request.get("difficulty") : "medium";
        Integer numQuestions = request.containsKey("numQuestions") ? (Integer) request.get("numQuestions") : 5;

        try {
            System.out.println("[QuizController] Generating quiz with AI for: " + subject + " - " + topic);

            // Call AI service to generate quiz
            Object aiResponse = aiQuizService.generateQuiz(subject, topic, difficulty, numQuestions);

            System.out.println("[QuizController] AI response received");

            // Parse AI response
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = (Map<String, Object>) aiResponse;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questions = (List<Map<String, Object>>) responseBody.get("questions");

            if (questions != null && !questions.isEmpty()) {
                // Create quiz entity
                Quiz quiz = new Quiz();
                quiz.setStudent(student);
                quiz.setSubject(subject);
                quiz.setTopic(topic);
                quiz.setTotalQuestions(numQuestions);
                quiz.setStatus("pending");

                quiz = quizRepository.save(quiz);

                // Create and save question entities
                List<Map<String, Object>> savedQuestions = new ArrayList<>();

                for (int i = 0; i < questions.size(); i++) {
                    Map<String, Object> q = questions.get(i);

                    // Extract options (handle both Map and List formats)
                    Object optionsObj = q.get("options");
                    Map<String, String> optionsMap = new HashMap<>();

                    if (optionsObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, String>> optionsList = (List<Map<String, String>>) optionsObj;
                        for (Map<String, String> opt : optionsList) {
                            optionsMap.put(opt.get("label"), opt.get("text"));
                        }
                    } else if (optionsObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> options = (Map<String, String>) optionsObj;
                        optionsMap = options;
                    }

                    QuizQuestion question = new QuizQuestion();
                    question.setQuiz(quiz);
                    question.setQuestion((String) q.get("questionText"));
                    question.setOptionA(optionsMap.get("A"));
                    question.setOptionB(optionsMap.get("B"));
                    question.setOptionC(optionsMap.get("C"));
                    question.setOptionD(optionsMap.get("D"));
                    question.setCorrectAnswer((String) q.get("correctAnswer"));
                    question.setQuestionOrder(i + 1);

                    question = quizQuestionRepository.save(question);

                    // Build response with question ID
                    Map<String, Object> questionResponse = new HashMap<>();
                    questionResponse.put("id", question.getId());
                    questionResponse.put("question", question.getQuestion());
                    questionResponse.put("options", optionsMap);
                    questionResponse.put("correctAnswer", question.getCorrectAnswer());
                    savedQuestions.add(questionResponse);
                }

                System.out.println(
                        "[QuizController] Quiz saved successfully with " + savedQuestions.size() + " questions");
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "quizId", quiz.getId(),
                        "questions", savedQuestions));
            }

            return ResponseEntity.status(500)
                    .body(Map.of("message", "Failed to generate quiz - no questions returned"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/student/{studentCin}")
    public ResponseEntity<?> getStudentQuizzes(@PathVariable String studentCin) {
        Optional<Student> studentOpt = studentRepository.findByCin(studentCin);
        if (!studentOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
        }
        Student student = studentOpt.get();

        List<Quiz> quizzes = quizRepository.findByStudent(student);
        return ResponseEntity.ok(quizzes);
    }

    @PostMapping("/submit/{quizId}")
    public ResponseEntity<?> submitQuiz(
            @PathVariable Long quizId,
            @RequestBody Map<String, Object> request) {

        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (!quizOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Quiz not found"));
        }

        Quiz quiz = quizOpt.get();
        @SuppressWarnings("unchecked")
        List<Map<String, String>> answers = (List<Map<String, String>>) request.get("answers");

        // Grade the quiz
        int correct = 0;
        int totalAnswered = 0;

        for (Map<String, String> answer : answers) {
            try {
                Long questionId = Long.parseLong(answer.get("questionId"));
                String studentAnswer = answer.get("answer");

                // Fetch question directly from repository
                Optional<QuizQuestion> questionOpt = quizQuestionRepository.findById(questionId);

                if (questionOpt.isPresent()) {
                    QuizQuestion question = questionOpt.get();
                    question.setStudentAnswer(studentAnswer);

                    // Compare answers (case-insensitive, trim whitespace)
                    boolean isCorrect = studentAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim());
                    question.setIsCorrect(isCorrect);

                    if (isCorrect) {
                        correct++;
                    }

                    quizQuestionRepository.save(question);
                    totalAnswered++;

                    System.out.println("Question " + questionId + ": Student=" + studentAnswer +
                            ", Correct=" + question.getCorrectAnswer() +
                            ", Match=" + isCorrect);
                }
            } catch (Exception e) {
                System.err.println("Error processing answer: " + e.getMessage());
            }
        }

        // Calculate score percentage
        double scorePercentage = (correct * 100.0) / quiz.getTotalQuestions();

        quiz.setCorrectAnswers(correct);
        quiz.setScore(scorePercentage);
        quiz.setStatus("completed");
        quiz.setCompletedAt(LocalDateTime.now());

        quizRepository.save(quiz);

        System.out.println("Quiz " + quizId + " graded: " + correct + "/" + quiz.getTotalQuestions() +
                " = " + scorePercentage + "%");

        // Create revision completion record
        RevisionCompletion completion = new RevisionCompletion();
        completion.setStudent(quiz.getStudent());
        completion.setSubject(quiz.getSubject());
        completion.setTopic(quiz.getTopic());
        completion.setQuiz(quiz);
        completion.setQuizScore(scorePercentage);

        revisionCompletionRepository.save(completion);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "score", scorePercentage,
                "correctAnswers", correct,
                "totalQuestions", quiz.getTotalQuestions(),
                "completionId", completion.getId()));
    }

    @PostMapping("/complete-revision/{studentCin}")
    public ResponseEntity<?> completeRevision(
            @PathVariable String studentCin,
            @RequestBody Map<String, Object> request) {

        Optional<Student> studentOpt = studentRepository.findByCin(studentCin);
        if (!studentOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
        }
        Student student = studentOpt.get();

        String subject = (String) request.get("subject");
        String topic = (String) request.get("topic");
        Long quizId = request.containsKey("quizId") ? ((Number) request.get("quizId")).longValue() : null;

        RevisionCompletion completion = new RevisionCompletion();
        completion.setStudent(student);
        completion.setSubject(subject);
        completion.setTopic(topic);

        if (quizId != null) {
            Quiz quiz = quizRepository.findById(quizId).orElse(null);
            if (quiz != null) {
                completion.setQuiz(quiz);
                completion.setQuizScore(quiz.getScore());
            }
        }

        if (request.containsKey("durationMinutes")) {
            completion.setRevisionDurationMinutes((Integer) request.get("durationMinutes"));
        }

        if (request.containsKey("notes")) {
            completion.setNotes((String) request.get("notes"));
        }

        revisionCompletionRepository.save(completion);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Revision marked as completed",
                "completionId", completion.getId()));
    }

    @GetMapping("/completed/{studentCin}")
    public ResponseEntity<?> getCompletedRevisions(@PathVariable String studentCin) {
        Optional<Student> studentOpt = studentRepository.findByCin(studentCin);
        if (!studentOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
        }
        Student student = studentOpt.get();

        List<RevisionCompletion> completions = revisionCompletionRepository.findByStudent(student);
        return ResponseEntity.ok(completions);
    }

    @GetMapping("/result/{quizId}")
    public ResponseEntity<?> getQuizResult(@PathVariable Long quizId) {
        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (!quizOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Quiz not found"));
        }

        Quiz quiz = quizOpt.get();

        // Fetch questions directly from repository (avoid lazy loading issues)
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizOrderByQuestionOrderAsc(quiz);

        // Build detailed result with questions and answers
        List<Map<String, Object>> questionResults = new ArrayList<>();
        for (QuizQuestion q : questions) {
            Map<String, Object> questionData = new HashMap<>();
            questionData.put("id", q.getId());
            questionData.put("question", q.getQuestion());
            questionData.put("options", Map.of(
                    "A", q.getOptionA(),
                    "B", q.getOptionB(),
                    "C", q.getOptionC(),
                    "D", q.getOptionD()));
            questionData.put("correctAnswer", q.getCorrectAnswer());
            questionData.put("studentAnswer", q.getStudentAnswer());

            // Explicitly convert Boolean to boolean primitive to ensure proper JSON
            // serialization
            Boolean isCorrectObj = q.getIsCorrect();
            boolean isCorrectValue = (isCorrectObj != null && isCorrectObj.booleanValue());
            questionData.put("isCorrect", isCorrectValue);

            System.out.println("Sending question " + q.getId() + ": isCorrect=" + isCorrectValue +
                    " (original: " + isCorrectObj + ", type: "
                    + (isCorrectObj != null ? isCorrectObj.getClass().getName() : "null") + ")" +
                    ", studentAnswer=" + q.getStudentAnswer() +
                    ", correctAnswer=" + q.getCorrectAnswer());

            questionResults.add(questionData);
        }

        System.out.println("Total questions in result: " + questionResults.size());

        // Use HashMap instead of Map.of() to allow null values
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("quizId", quiz.getId());
        response.put("subject", quiz.getSubject());
        response.put("topic", quiz.getTopic());
        response.put("score", quiz.getScore() != null ? quiz.getScore() : 0.0);
        response.put("correctAnswers", quiz.getCorrectAnswers() != null ? quiz.getCorrectAnswers() : 0);
        response.put("totalQuestions", quiz.getTotalQuestions());
        response.put("status", quiz.getStatus());
        response.put("completedAt", quiz.getCompletedAt() != null ? quiz.getCompletedAt().toString() : null);
        response.put("questions", questionResults);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{studentCin}")
    public ResponseEntity<?> getQuizHistory(@PathVariable String studentCin) {
        Optional<Student> studentOpt = studentRepository.findByCin(studentCin);
        if (!studentOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
        }

        Student student = studentOpt.get();

        // Get all completed quizzes for this student, ordered by completion date
        // (newest first)
        List<Quiz> quizzes = quizRepository.findByStudentAndStatusOrderByCompletedAtDesc(student, "completed");

        List<Map<String, Object>> quizHistory = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            Map<String, Object> quizData = new HashMap<>();
            quizData.put("id", quiz.getId());
            quizData.put("subject", quiz.getSubject());
            quizData.put("topic", quiz.getTopic());
            quizData.put("score", quiz.getScore() != null ? quiz.getScore() : 0.0);
            quizData.put("correctAnswers", quiz.getCorrectAnswers() != null ? quiz.getCorrectAnswers() : 0);
            quizData.put("totalQuestions", quiz.getTotalQuestions());
            quizData.put("completedAt", quiz.getCompletedAt() != null ? quiz.getCompletedAt().toString() : null);
            quizData.put("generatedAt", quiz.getGeneratedAt() != null ? quiz.getGeneratedAt().toString() : null);

            quizHistory.add(quizData);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "quizzes", quizHistory,
                "total", quizHistory.size()));
    }
}
