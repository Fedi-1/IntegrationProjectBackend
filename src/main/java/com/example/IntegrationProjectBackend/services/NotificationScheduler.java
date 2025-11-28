package com.example.IntegrationProjectBackend.services;

import com.example.IntegrationProjectBackend.models.*;
import com.example.IntegrationProjectBackend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class NotificationScheduler {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private GeneratedScheduleRepository generatedScheduleRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private SendGridEmailService sendGridEmailService;

    /**
     * Runs every 15 minutes to check if students have revision sessions ending soon
     * Sends reminder to student 10 minutes before session ends
     */
    @Scheduled(cron = "0 */15 * * * *")
    public void checkUpcomingRevisionSessions() {
        System.out.println("🔔 [" + LocalDateTime.now() + "] Checking for revision sessions ending soon...");

        try {
            List<Student> allStudents = studentRepository.findAll();
            String today = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            LocalDateTime now = LocalDateTime.now();
            System.out.println("🔍 Looking for revision sessions on: " + today);

            for (Student student : allStudents) {
                // Use custom query that handles NULL as uncompleted
                List<GeneratedSchedule> todaysTasks = generatedScheduleRepository
                        .findUncompletedByStudentAndDay(student, today);

                for (GeneratedSchedule task : todaysTasks) {
                    if (task.getActivity() != null &&
                            (task.getActivity().toLowerCase().contains("revision") ||
                                    task.getActivity().toLowerCase().contains("study"))) {

                        String timeSlot = task.getTimeSlot();
                        if (timeSlot != null && timeSlot.contains("-")) {
                            String[] times = timeSlot.split("-");
                            String endTime = times[1].trim();

                            try {
                                String[] hourMin = endTime.split(":");
                                int endHour = Integer.parseInt(hourMin[0]);
                                int endMinute = Integer.parseInt(hourMin[1]);

                                LocalDateTime sessionEnd = now.toLocalDate().atTime(endHour, endMinute);
                                long minutesUntilEnd = java.time.Duration.between(now, sessionEnd).toMinutes();

                                if (minutesUntilEnd > 5 && minutesUntilEnd <= 15) {
                                    String studentName = student.getFirstName() + " " + student.getLastName();
                                    String subject = task.getSubject() != null ? task.getSubject().getName()
                                            : "your subject";
                                    String topic = task.getTopic() != null ? task.getTopic() : "";

                                    sendGridEmailService.sendRevisionEndingReminder(
                                            student.getEmail(),
                                            studentName,
                                            subject,
                                            topic,
                                            (int) minutesUntilEnd);

                                    System.out.println("✅ Sent revision ending reminder to " + studentName +
                                            " (" + subject + " - ends in " + minutesUntilEnd + " min)");
                                }
                            } catch (Exception e) {
                                continue;
                            }
                        }
                    }
                }
            }

            System.out.println("✅ Revision session check completed");
        } catch (Exception e) {
            System.err.println("❌ Error in revision session check: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Scheduled(cron = "0 0 23 * * *") // 23:00 every day
    public void checkUnfinishedHomework() {
        System.out.println("🔔 [" + LocalDateTime.now() + "] Checking for unfinished homework...");

        try {
            List<Student> allStudents = studentRepository.findAll();
            String today = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            System.out.println("🔍 Looking for unfinished homework on: " + today);

            for (Student student : allStudents) {
                // Use custom query that handles NULL as uncompleted
                List<GeneratedSchedule> unfinishedTasks = generatedScheduleRepository
                        .findUncompletedByStudentAndDay(student, today);

                if (unfinishedTasks.isEmpty()) {
                    continue;
                }

                // Build task list
                StringBuilder taskList = new StringBuilder();
                for (GeneratedSchedule task : unfinishedTasks) {
                    taskList.append("- ")
                            .append(task.getTimeSlot())
                            .append(": ")
                            .append(task.getActivity());
                    if (task.getSubject() != null) {
                        taskList.append(" (").append(task.getSubject().getName()).append(")");
                    }
                    if (task.getTopic() != null) {
                        taskList.append(" - ").append(task.getTopic());
                    }
                    taskList.append("\n");
                }

                String studentName = student.getFirstName() + " " + student.getLastName();

                // Send to both parent AND student
                Parent parent = student.getParent();

                // Always send to student
                sendGridEmailService.sendHomeworkAlert(
                        student.getEmail(),
                        studentName,
                        studentName,
                        unfinishedTasks.size(),
                        taskList.toString());
                System.out.println("⚠️ Sent homework alert to student " + studentName +
                        " (" + unfinishedTasks.size() + " tasks not completed)");

                // Also send to parent if available
                if (parent != null && parent.getEmail() != null && !parent.getEmail().isEmpty()) {
                    String parentName = parent.getFirstName() + " " + parent.getLastName();
                    sendGridEmailService.sendHomeworkAlert(
                            parent.getEmail(),
                            parentName,
                            studentName,
                            unfinishedTasks.size(),
                            taskList.toString());
                    System.out.println("⚠️ Also sent homework alert to parent " + parentName);
                }
            }

            System.out.println("✅ Unfinished homework check completed");
        } catch (Exception e) {
            System.err.println("❌ Error in unfinished homework check: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check for quiz scores and notify parents
     * Runs every hour to catch newly completed quizzes
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkQuizScores() {
        System.out.println("🔔 [" + LocalDateTime.now() + "] Running quiz score check...");

        try {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            List<Quiz> recentQuizzes = quizRepository.findByStatusAndCompletedAtAfter("completed", oneHourAgo);

            for (Quiz quiz : recentQuizzes) {
                Student student = quiz.getStudent();
                Double score = quiz.getScore();

                if (score == null) {
                    continue;
                }

                String studentName = student.getFirstName() + " " + student.getLastName();
                String quizTitle = quiz.getSubject() + " - " + quiz.getTopic();

                // Try to send to parent first, otherwise send to student
                Parent parent = student.getParent();
                if (parent != null && parent.getEmail() != null && !parent.getEmail().isEmpty()) {
                    // Send to parent
                    String parentName = parent.getFirstName() + " " + parent.getLastName();
                    sendGridEmailService.sendQuizAlert(
                            parent.getEmail(),
                            parentName,
                            studentName,
                            quizTitle,
                            score,
                            100.0);
                    String emoji = score >= 70 ? "✅" : "⚠️";
                    System.out.println(emoji + " Sent quiz notification to parent of " + studentName +
                            " (score: " + score + "%)");
                } else {
                    // No parent or parent email unavailable - send to student
                    sendGridEmailService.sendQuizAlert(
                            student.getEmail(),
                            studentName,
                            studentName,
                            quizTitle,
                            score,
                            100.0);
                    String emoji = score >= 70 ? "✅" : "⚠️";
                    System.out.println(emoji + " Sent quiz notification directly to student " + studentName +
                            " (score: " + score + "%)");
                }
            }

            System.out.println("✅ Quiz score check completed");
        } catch (Exception e) {
            System.err.println("❌ Error in quiz score check: " + e.getMessage());
            e.printStackTrace();
        }
    }
}