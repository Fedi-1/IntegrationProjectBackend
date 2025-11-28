package com.example.IntegrationProjectBackend.controllers;

import com.example.IntegrationProjectBackend.models.GeneratedSchedule;
import com.example.IntegrationProjectBackend.models.Student;
import com.example.IntegrationProjectBackend.repositories.GeneratedScheduleRepository;
import com.example.IntegrationProjectBackend.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;

@RestController
@RequestMapping("/api/daily-schedule")
@CrossOrigin(origins = "*")
public class DailyScheduleController {

    @Autowired
    private GeneratedScheduleRepository generatedScheduleRepository;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * Helper method to check if a session was completed late
     * A session is considered late if completed more than 5 minutes after scheduled
     * end time
     */
    private boolean isCompletedLate(GeneratedSchedule schedule) {
        if (schedule.getCompletedAt() == null || !Boolean.TRUE.equals(schedule.getCompleted())) {
            return false;
        }

        try {
            // Parse time slot (e.g., "18:00-18:30")
            String timeSlot = schedule.getTimeSlot();
            String[] times = timeSlot.split("-");
            if (times.length != 2) {
                return false;
            }

            // Get end time
            String endTimeStr = times[1].trim();
            LocalTime endTime = LocalTime.parse(endTimeStr);

            // Get completion time
            LocalTime completedTime = schedule.getCompletedAt().toLocalTime();

            // Add 5 minute grace period to end time
            LocalTime graceEndTime = endTime.plusMinutes(5);

            // Check if completed after grace period
            return completedTime.isAfter(graceEndTime);

        } catch (Exception e) {
            // If parsing fails, return false
            return false;
        }
    }

    @GetMapping("/today/{studentCin}")
    public ResponseEntity<?> getTodaySchedule(@PathVariable String studentCin) {
        Optional<Student> studentOpt = studentRepository.findByCin(studentCin);
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

        // Filter out breaks - only include actual study sessions
        List<GeneratedSchedule> studySessions = todaySchedules.stream()
                .filter(s -> !s.getActivity().equalsIgnoreCase("Break") &&
                        !s.getActivity().equalsIgnoreCase("PAUSE"))
                .collect(java.util.stream.Collectors.toList());

        // Build response
        List<Map<String, Object>> scheduleItems = new ArrayList<>();
        for (GeneratedSchedule schedule : studySessions) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", schedule.getId());
            item.put("timeSlot", schedule.getTimeSlot());
            item.put("activity", schedule.getActivity());
            item.put("durationMinutes", schedule.getDurationMinutes());
            item.put("topic", schedule.getTopic());

            boolean isCompleted = schedule.getCompleted() != null ? schedule.getCompleted() : false;
            item.put("completed", isCompleted);
            item.put("completedAt", schedule.getCompletedAt() != null ? schedule.getCompletedAt().toString() : null);

            // Check if completed late
            boolean isLate = isCompletedLate(schedule);
            item.put("isLate", isLate);

            if (schedule.getSubject() != null) {
                item.put("subjectId", schedule.getSubject().getId());
                item.put("subjectName", schedule.getSubject().getName());
            }

            scheduleItems.add(item);
        }

        // Calculate completion stats (only for study sessions, not breaks)
        long totalSessions = studySessions.size();
        long completedSessions = studySessions.stream()
                .filter(s -> s.getCompleted() != null && s.getCompleted())
                .count();
        long lateSessions = studySessions.stream()
                .filter(this::isCompletedLate)
                .count();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "day", currentDay,
                "date", today.toString(),
                "schedules", scheduleItems,
                "stats", Map.of(
                        "total", totalSessions,
                        "completed", completedSessions,
                        "remaining", totalSessions - completedSessions,
                        "late", lateSessions,
                        "completionPercentage", totalSessions > 0 ? (completedSessions * 100.0 / totalSessions) : 0)));
    }

    @PostMapping("/complete/{scheduleId}")
    public ResponseEntity<?> markAsCompleted(@PathVariable Long scheduleId) {
        Optional<GeneratedSchedule> scheduleOpt = generatedScheduleRepository.findById(scheduleId);
        if (!scheduleOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Schedule not found"));
        }

        GeneratedSchedule schedule = scheduleOpt.get();

        // Mark as completed
        schedule.setCompleted(true);
        schedule.setCompletedAt(LocalDateTime.now());
        generatedScheduleRepository.save(schedule);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Session marked as completed",
                "completedAt", schedule.getCompletedAt().toString()));
    }

    @PostMapping("/uncomplete/{scheduleId}")
    public ResponseEntity<?> markAsUncompleted(@PathVariable Long scheduleId) {
        Optional<GeneratedSchedule> scheduleOpt = generatedScheduleRepository.findById(scheduleId);
        if (!scheduleOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Schedule not found"));
        }

        GeneratedSchedule schedule = scheduleOpt.get();

        // Mark as uncompleted
        schedule.setCompleted(false);
        schedule.setCompletedAt(null);
        generatedScheduleRepository.save(schedule);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Session marked as not completed"));
    }

    @GetMapping("/week/{studentCin}")
    public ResponseEntity<?> getWeekSchedule(@PathVariable String studentCin) {
        Optional<Student> studentOpt = studentRepository.findByCin(studentCin);
        if (!studentOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
        }

        Student student = studentOpt.get();

        // Get all schedules for the week
        List<GeneratedSchedule> allSchedules = generatedScheduleRepository
                .findByStudentOrderByDayAscTimeSlotAsc(student);

        // Group by day
        Map<String, List<Map<String, Object>>> weekSchedule = new LinkedHashMap<>();
        String[] daysOfWeek = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

        for (String day : daysOfWeek) {
            List<Map<String, Object>> daySchedules = new ArrayList<>();

            for (GeneratedSchedule schedule : allSchedules) {
                if (schedule.getDay().equals(day)) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", schedule.getId());
                    item.put("timeSlot", schedule.getTimeSlot());
                    item.put("activity", schedule.getActivity());
                    item.put("durationMinutes", schedule.getDurationMinutes());
                    item.put("topic", schedule.getTopic());
                    item.put("completed", schedule.getCompleted() != null ? schedule.getCompleted() : false);

                    if (schedule.getSubject() != null) {
                        item.put("subjectName", schedule.getSubject().getName());
                    }

                    daySchedules.add(item);
                }
            }

            if (!daySchedules.isEmpty()) {
                weekSchedule.put(day, daySchedules);
            }
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "weekSchedule", weekSchedule));
    }
}