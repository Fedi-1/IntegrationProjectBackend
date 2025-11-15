package com.example.IntegrationProjectBackend.dtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example request payloads for testing the Schedule Generator API
 */
public class ScheduleGenerationRequestExamples {

    /**
     * Basic example with minimal data
     */
    public static ScheduleGenerationRequest getBasicExample() {
        ScheduleGenerationRequest request = new ScheduleGenerationRequest();
        request.setName("Ahmed Hassan");
        request.setAge(18);

        List<ScheduleGenerationRequest.SubjectInfo> subjects = new ArrayList<>();

        ScheduleGenerationRequest.SubjectInfo math = new ScheduleGenerationRequest.SubjectInfo();
        math.setName("Math");
        math.setDifficulty("hard");
        math.setHoursPerWeek(10);
        subjects.add(math);

        ScheduleGenerationRequest.SubjectInfo physics = new ScheduleGenerationRequest.SubjectInfo();
        physics.setName("Physics");
        physics.setDifficulty("medium");
        physics.setHoursPerWeek(8);
        subjects.add(physics);

        request.setSubjects(subjects);
        return request;
    }

    /**
     * Complete example with all fields
     */
    public static ScheduleGenerationRequest getCompleteExample() {
        ScheduleGenerationRequest request = new ScheduleGenerationRequest();
        request.setName("Sara Mohamed");
        request.setAge(19);

        // Subjects
        List<ScheduleGenerationRequest.SubjectInfo> subjects = new ArrayList<>();

        ScheduleGenerationRequest.SubjectInfo math = new ScheduleGenerationRequest.SubjectInfo();
        math.setName("Mathematics");
        math.setDifficulty("hard");
        math.setHoursPerWeek(12);
        subjects.add(math);

        ScheduleGenerationRequest.SubjectInfo physics = new ScheduleGenerationRequest.SubjectInfo();
        physics.setName("Physics");
        physics.setDifficulty("medium");
        physics.setHoursPerWeek(10);
        subjects.add(physics);

        ScheduleGenerationRequest.SubjectInfo english = new ScheduleGenerationRequest.SubjectInfo();
        english.setName("English");
        english.setDifficulty("easy");
        english.setHoursPerWeek(6);
        subjects.add(english);

        request.setSubjects(subjects);

        // Exam dates
        Map<String, String> examDates = new HashMap<>();
        examDates.put("Mathematics", "2025-12-15");
        examDates.put("Physics", "2025-12-18");
        examDates.put("English", "2025-12-20");
        request.setExamDates(examDates);

        // Learning preferences
        Map<String, Object> learningPreferences = new HashMap<>();
        learningPreferences.put("preferredTime", "morning");
        learningPreferences.put("studyStyle", "visual");
        learningPreferences.put("breakFrequency", 60);
        request.setLearningPreferences(learningPreferences);

        // Weekly availability
        // Set maxStudyDuration (maximum hours per day)
        request.setMaxStudyDuration(4); // 4 hours per day for home study

        // Constraints
        Map<String, Object> constraints = new HashMap<>();
        constraints.put("maxStudyHoursPerDay", 6);
        constraints.put("minBreakDuration", 10);
        constraints.put("maxConsecutiveStudyHours", 2);
        request.setConstraints(constraints);

        return request;
    }

    /**
     * Example for a student preparing for exams
     */
    public static ScheduleGenerationRequest getExamPrepExample() {
        ScheduleGenerationRequest request = new ScheduleGenerationRequest();
        request.setName("Youssef Ali");
        request.setAge(20);

        List<ScheduleGenerationRequest.SubjectInfo> subjects = new ArrayList<>();

        ScheduleGenerationRequest.SubjectInfo calculus = new ScheduleGenerationRequest.SubjectInfo();
        calculus.setName("Calculus");
        calculus.setDifficulty("hard");
        calculus.setHoursPerWeek(15);
        subjects.add(calculus);

        ScheduleGenerationRequest.SubjectInfo chemistry = new ScheduleGenerationRequest.SubjectInfo();
        chemistry.setName("Chemistry");
        chemistry.setDifficulty("hard");
        chemistry.setHoursPerWeek(12);
        subjects.add(chemistry);

        ScheduleGenerationRequest.SubjectInfo biology = new ScheduleGenerationRequest.SubjectInfo();
        biology.setName("Biology");
        biology.setDifficulty("medium");
        biology.setHoursPerWeek(10);
        subjects.add(biology);

        request.setSubjects(subjects);

        Map<String, String> examDates = new HashMap<>();
        examDates.put("Calculus", "2025-11-20");
        examDates.put("Chemistry", "2025-11-22");
        examDates.put("Biology", "2025-11-25");
        request.setExamDates(examDates);

        Map<String, Object> learningPreferences = new HashMap<>();
        learningPreferences.put("focusOnWeakTopics", true);
        learningPreferences.put("includeMockTests", true);
        request.setLearningPreferences(learningPreferences);

        return request;
    }

    /**
     * Print example JSON for testing with Postman or curl
     */
    public static void main(String[] args) {
        System.out.println("=== BASIC EXAMPLE ===");
        System.out.println("POST https://integrationprojectbackend.onrender.com/api/schedule/generate/STUDENT_CIN");
        System.out.println("Content-Type: application/json");
        System.out.println();
        System.out.println("{\n" +
                "  \"name\": \"Ahmed Hassan\",\n" +
                "  \"age\": 18,\n" +
                "  \"subjects\": [\n" +
                "    {\n" +
                "      \"name\": \"Math\",\n" +
                "      \"difficulty\": \"hard\",\n" +
                "      \"hoursPerWeek\": 10\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Physics\",\n" +
                "      \"difficulty\": \"medium\",\n" +
                "      \"hoursPerWeek\": 8\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        System.out.println("\n\n=== CURL COMMAND ===");
        System.out.println(
                "curl -X POST https://integrationprojectbackend.onrender.com/api/schedule/generate/STUDENT_CIN \\");
        System.out.println("  -H \"Content-Type: application/json\" \\");
        System.out.println(
                "  -d '{\"name\":\"Ahmed Hassan\",\"age\":18,\"subjects\":[{\"name\":\"Math\",\"difficulty\":\"hard\",\"hoursPerWeek\":10},{\"name\":\"Physics\",\"difficulty\":\"medium\",\"hoursPerWeek\":8}]}'");
    }
}
