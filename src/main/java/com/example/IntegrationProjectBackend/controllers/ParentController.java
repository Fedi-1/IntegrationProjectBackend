package com.example.IntegrationProjectBackend.controllers;

import com.example.IntegrationProjectBackend.models.*;
import com.example.IntegrationProjectBackend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/parent")
@CrossOrigin(origins = "*")
public class ParentController {

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private GeneratedScheduleRepository generatedScheduleRepository;

    @Autowired
    private QuizRepository quizRepository;

    /**
     * Get all children for a parent
     * GET /api/parent/{parentCin}/children
     */
    @GetMapping("/{parentCin}/children")
    public ResponseEntity<?> getChildren(@PathVariable String parentCin) {
        Optional<Parent> parentOpt = parentRepository.findByCin(parentCin);
        if (!parentOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Parent not found"));
        }

        Parent parent = parentOpt.get();
        List<Student> children = studentRepository.findByParent(parent);

        List<Map<String, Object>> childrenData = children.stream().map(child -> {
            Map<String, Object> childInfo = new HashMap<>();
            childInfo.put("id", child.getId());
            childInfo.put("cin", child.getCin());
            childInfo.put("firstName", child.getFirstName());
            childInfo.put("lastName", child.getLastName());
            childInfo.put("email", child.getEmail());
            childInfo.put("age", child.getAge());
            childInfo.put("phoneNumber", child.getPhoneNumber());
            return childInfo;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "children", childrenData));
    }

    /**
     * Get child's schedule
     * GET /api/parent/child/{childCin}/schedule
     */
    @GetMapping("/child/{childCin}/schedule")
    public ResponseEntity<?> getChildSchedule(@PathVariable String childCin) {
        Optional<Student> studentOpt = studentRepository.findByCin(childCin);
        if (!studentOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
        }

        Student student = studentOpt.get();
        List<GeneratedSchedule> schedules = generatedScheduleRepository
                .findByStudentOrderByDayAscTimeSlotAsc(student);

        // Group by day
        Map<String, List<Map<String, Object>>> weekSchedule = new LinkedHashMap<>();
        String[] daysOfWeek = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

        for (String day : daysOfWeek) {
            List<Map<String, Object>> daySchedules = new ArrayList<>();

            for (GeneratedSchedule schedule : schedules) {
                if (schedule.getDay().equals(day)) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", schedule.getId());
                    item.put("timeSlot", schedule.getTimeSlot());
                    item.put("activity", schedule.getActivity());
                    item.put("durationMinutes", schedule.getDurationMinutes());
                    item.put("completed", schedule.getCompleted());

                    if (schedule.getSubject() != null) {
                        item.put("subjectName", schedule.getSubject().getName());
                    }

                    daySchedules.add(item);
                }
            }

            weekSchedule.put(day, daySchedules);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "studentName", student.getFirstName() + " " + student.getLastName(),
                "weekSchedule", weekSchedule));
    }

    /**
     * Get child's today schedule with completion status
     * GET /api/parent/child/{childCin}/today
     */
    @GetMapping("/child/{childCin}/today")
    public ResponseEntity<?> getChildTodaySchedule(@PathVariable String childCin) {
        Optional<Student> studentOpt = studentRepository.findByCin(childCin);
        if (!studentOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
        }

        Student student = studentOpt.get();

        // Get current day of week
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        String currentDay = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        // Get schedules for today
        List<GeneratedSchedule> todaySchedules = generatedScheduleRepository
                .findByStudentAndDayOrderByTimeSlotAsc(student, currentDay);

        // Build response
        List<Map<String, Object>> scheduleItems = new ArrayList<>();
        for (GeneratedSchedule schedule : todaySchedules) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", schedule.getId());
            item.put("timeSlot", schedule.getTimeSlot());
            item.put("activity", schedule.getActivity());
            item.put("durationMinutes", schedule.getDurationMinutes());
            item.put("completed", schedule.getCompleted() != null ? schedule.getCompleted() : false);
            item.put("completedAt", schedule.getCompletedAt());

            if (schedule.getSubject() != null) {
                item.put("subjectName", schedule.getSubject().getName());
            }

            scheduleItems.add(item);
        }

        // Calculate stats
        long totalSessions = todaySchedules.size();
        long completedSessions = todaySchedules.stream()
                .filter(s -> s.getCompleted() != null && s.getCompleted())
                .count();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "studentName", student.getFirstName() + " " + student.getLastName(),
                "day", currentDay,
                "date", today.toString(),
                "schedules", scheduleItems,
                "stats", Map.of(
                        "total", totalSessions,
                        "completed", completedSessions,
                        "remaining", totalSessions - completedSessions,
                        "completionPercentage",
                        totalSessions > 0 ? (completedSessions * 100.0 / totalSessions) : 0)));
    }

    /**
     * Get child's quiz performance/history
     * GET /api/parent/child/{childCin}/performance
     */
    @GetMapping("/child/{childCin}/performance")
    public ResponseEntity<?> getChildPerformance(@PathVariable String childCin) {
        try {
            Optional<Student> studentOpt = studentRepository.findByCin(childCin);
            if (!studentOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
            }

            Student student = studentOpt.get();

            // Get all completed quizzes - with null safety
            List<Quiz> completedQuizzes = quizRepository.findByStudentAndStatusOrderByCompletedAtDesc(student,
                    "completed");
            if (completedQuizzes == null) {
                completedQuizzes = new ArrayList<>();
            }

            // Calculate overall stats
            double totalScore = 0;
            int quizCount = completedQuizzes.size();
            Map<String, List<Double>> subjectScores = new HashMap<>();
            Map<String, Integer> subjectCounts = new HashMap<>();

            List<Map<String, Object>> recentQuizzes = new ArrayList<>();

            for (Quiz quiz : completedQuizzes) {
                if (quiz == null)
                    continue;

                double score = quiz.getScore() != null ? quiz.getScore() : 0;
                totalScore += score;

                // Track by subject - null safety
                String subject = quiz.getSubject() != null ? quiz.getSubject() : "Unknown";
                subjectScores.computeIfAbsent(subject, k -> new ArrayList<>()).add(score);
                subjectCounts.put(subject, subjectCounts.getOrDefault(subject, 0) + 1);

                // Add to recent list (limit to 10)
                if (recentQuizzes.size() < 10) {
                    Map<String, Object> quizData = new HashMap<>();
                    quizData.put("id", quiz.getId());
                    quizData.put("subject", quiz.getSubject() != null ? quiz.getSubject() : "Unknown");
                    quizData.put("topic", quiz.getTopic() != null ? quiz.getTopic() : "N/A");
                    quizData.put("score", quiz.getScore() != null ? quiz.getScore() : 0);
                    quizData.put("totalQuestions", quiz.getTotalQuestions());
                    quizData.put("completedAt",
                            quiz.getCompletedAt() != null ? quiz.getCompletedAt().toString() : null);
                    recentQuizzes.add(quizData);
                }
            }

            // Calculate averages by subject
            Map<String, Double> subjectAverages = new HashMap<>();
            for (Map.Entry<String, List<Double>> entry : subjectScores.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    subjectAverages.put(entry.getKey(), avg);
                }
            }

            // Detect difficulties (subjects with average < 60%)
            List<String> difficulties = subjectAverages.entrySet().stream()
                    .filter(e -> e.getValue() != null && e.getValue() < 60.0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "studentName", student.getFirstName() + " " + student.getLastName(),
                    "overallStats", Map.of(
                            "totalQuizzes", quizCount,
                            "averageScore", quizCount > 0 ? totalScore / quizCount : 0,
                            "totalSubjects", subjectScores.size()),
                    "subjectPerformance", subjectAverages != null ? subjectAverages : new HashMap<>(),
                    "difficulties", difficulties != null ? difficulties : new ArrayList<>(),
                    "recentQuizzes", recentQuizzes != null ? recentQuizzes : new ArrayList<>()));

        } catch (Exception e) {
            // Log the error and return a meaningful response
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error loading performance data: " + e.getMessage(),
                    "error", e.getClass().getName()));
        }
    }

    /**
     * Get alerts for child (missed tasks, low performance)
     * GET /api/parent/child/{childCin}/alerts
     */
    @GetMapping("/child/{childCin}/alerts")
    public ResponseEntity<?> getChildAlerts(@PathVariable String childCin) {
        Optional<Student> studentOpt = studentRepository.findByCin(childCin);
        if (!studentOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
        }

        Student student = studentOpt.get();
        List<Map<String, Object>> alerts = new ArrayList<>();

        // Check today's incomplete tasks
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        String currentDay = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        List<GeneratedSchedule> todaySchedules = generatedScheduleRepository
                .findByStudentAndDayOrderByTimeSlotAsc(student, currentDay);

        long incompleteTasks = todaySchedules.stream()
                .filter(s -> s.getCompleted() == null || !s.getCompleted())
                .count();

        if (incompleteTasks > 0) {
            alerts.add(Map.of(
                    "type", "warning",
                    "title", "Incomplete Tasks",
                    "message",
                    student.getFirstName() + " has " + incompleteTasks + " incomplete tasks for today",
                    "timestamp", LocalDateTime.now().toString()));
        }

        // Check recent quiz performance
        List<Quiz> recentQuizzes = quizRepository
                .findByStudentAndStatusOrderByCompletedAtDesc(student, "completed")
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        long poorPerformance = recentQuizzes.stream()
                .filter(q -> q.getScore() != null && q.getScore() < 50)
                .count();

        if (poorPerformance >= 2) {
            alerts.add(Map.of(
                    "type", "danger",
                    "title", "Low Performance Alert",
                    "message",
                    student.getFirstName() + " has scored below 50% in " + poorPerformance + " recent quizzes",
                    "timestamp", LocalDateTime.now().toString()));
        }

        // Check schedule adherence for past week
        LocalDate weekAgo = today.minusDays(7);
        List<GeneratedSchedule> pastWeekSchedules = generatedScheduleRepository
                .findByStudentOrderByDayAscTimeSlotAsc(student);

        long totalPastTasks = pastWeekSchedules.stream()
                .filter(s -> s.getCompletedAt() != null &&
                        s.getCompletedAt().toLocalDate().isAfter(weekAgo))
                .count();

        long completedPastTasks = pastWeekSchedules.stream()
                .filter(s -> s.getCompleted() != null && s.getCompleted() &&
                        s.getCompletedAt() != null &&
                        s.getCompletedAt().toLocalDate().isAfter(weekAgo))
                .count();

        double completionRate = totalPastTasks > 0 ? (completedPastTasks * 100.0 / totalPastTasks) : 100;

        if (completionRate < 50) {
            alerts.add(Map.of(
                    "type", "warning",
                    "title", "Low Schedule Adherence",
                    "message",
                    student.getFirstName() + " completed only " + String.format("%.1f", completionRate)
                            + "% of tasks this week",
                    "timestamp", LocalDateTime.now().toString()));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "studentName", student.getFirstName() + " " + student.getLastName(),
                "alerts", alerts,
                "summary", Map.of(
                        "totalAlerts", alerts.size(),
                        "todayIncomplete", incompleteTasks,
                        "weeklyCompletionRate", completionRate)));
    }
}
