package com.example.IntegrationProjectBackend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class AIQuizService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AIQuizService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate a personalized quiz using AI
     * @param subject Subject name
     * @param topic Specific topic for the quiz
     * @param difficulty Difficulty level (easy, medium, hard)
     * @param questionCount Number of questions to generate
     * @return JSON object with generated quiz
     */
    public Object generateQuiz(String subject, String topic, String difficulty, int questionCount) {
        System.out.println("[AIQuizService] Starting quiz generation for: " + subject + " - " + topic);

        String prompt = String.format("""
            You are an expert educator. Generate a quiz with %d questions on the following topic:
            
            SUBJECT: %s
            TOPIC: %s
            DIFFICULTY: %s
            
            REQUIREMENTS:
            - Create exactly %d multiple-choice questions
            - Each question should have 4 options (A, B, C, D)
            - Only one correct answer per question
            - Questions should test understanding, not just memorization
            - Include a mix of conceptual and practical questions
            - Provide clear, concise question text
            - Make distractors (wrong answers) plausible but clearly incorrect
            
            Respond ONLY with valid JSON, no markdown, no code blocks, no additional text:
            
            {
              "quizMetadata": {
                "subject": "%s",
                "topic": "%s",
                "difficulty": "%s",
                "totalQuestions": %d,
                "estimatedTime": "15 minutes"
              },
              "questions": [
                {
                  "id": 1,
                  "questionText": "Question text here?",
                  "options": [
                    {"label": "A", "text": "Option A text"},
                    {"label": "B", "text": "Option B text"},
                    {"label": "C", "text": "Option C text"},
                    {"label": "D", "text": "Option D text"}
                  ],
                  "correctAnswer": "A",
                  "explanation": "Brief explanation why this is correct"
                }
              ]
            }
            """, questionCount, subject, topic, difficulty, questionCount, subject, topic, difficulty, questionCount);

        try {
            var response = chatClient.prompt()
                    .user(prompt)
                    .call();

            String jsonString = Objects.requireNonNull(response.content());
            System.out.println("[AIQuizService] AI response received");

            String cleanJson = extractJsonFromResponse(jsonString);
            Object parsedJson = objectMapper.readValue(cleanJson, Object.class);

            System.out.println("[AIQuizService] Quiz generated successfully");
            return parsedJson;

        } catch (Exception e) {
            System.err.println("[AIQuizService] Error generating quiz: " + e.getMessage());
            throw new RuntimeException("Error generating quiz with AI: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a quiz based on student's weak areas
     * @param weakTopics List of topics where student performed poorly
     * @param questionCount Number of questions
     * @return JSON object with personalized quiz
     */
    public Object generateAdaptiveQuiz(List<Map<String, Object>> weakTopics, int questionCount) {
        System.out.println("[AIQuizService] Starting adaptive quiz generation");

        String weakTopicsJson = convertToJson(weakTopics);

        String prompt = String.format("""
            You are an expert educator. Generate an adaptive quiz focusing on the student's weak areas:
            
            WEAK TOPICS:
            %s
            
            REQUIREMENTS:
            - Create exactly %d questions focusing on these weak topics
            - Start with easier questions to build confidence
            - Progressively increase difficulty
            - Include questions that address common misconceptions
            - Provide detailed explanations for learning
            
            Respond ONLY with valid JSON, no markdown, no code blocks, no additional text:
            
            {
              "quizMetadata": {
                "quizType": "adaptive",
                "focusAreas": [],
                "totalQuestions": %d,
                "difficulty": "mixed"
              },
              "questions": [
                {
                  "id": 1,
                  "questionText": "Question text?",
                  "options": [
                    {"label": "A", "text": "Option A"},
                    {"label": "B", "text": "Option B"},
                    {"label": "C", "text": "Option C"},
                    {"label": "D", "text": "Option D"}
                  ],
                  "correctAnswer": "A",
                  "explanation": "Detailed explanation",
                  "relatedTopic": "Topic name"
                }
              ]
            }
            """, weakTopicsJson, questionCount, questionCount);

        try {
            var response = chatClient.prompt()
                    .user(prompt)
                    .call();

            String jsonString = Objects.requireNonNull(response.content());
            String cleanJson = extractJsonFromResponse(jsonString);
            Object parsedJson = objectMapper.readValue(cleanJson, Object.class);

            System.out.println("[AIQuizService] Adaptive quiz generated successfully");
            return parsedJson;

        } catch (Exception e) {
            System.err.println("[AIQuizService] Error generating adaptive quiz: " + e.getMessage());
            throw new RuntimeException("Error generating adaptive quiz: " + e.getMessage(), e);
        }
    }

    private String convertToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "{}";
        }

        String cleaned = response.trim();

        // Remove markdown code blocks
        Pattern codeBlockPattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```\\s*", Pattern.DOTALL);
        var matcher = codeBlockPattern.matcher(cleaned);
        if (matcher.find()) {
            cleaned = matcher.group(1).trim();
        }

        // Extract JSON object
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}') + 1;
        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end).trim();
        }

        // Remove comments
        cleaned = cleaned.replaceAll("/\\*.*?\\*/", "")
                        .replaceAll("//.*", "")
                        .trim();

        return cleaned.isEmpty() ? "{}" : cleaned;
    }
}
