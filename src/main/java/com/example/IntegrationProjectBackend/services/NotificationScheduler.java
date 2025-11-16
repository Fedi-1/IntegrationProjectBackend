package com.example.IntegrationProjectBackend.services;

import com.example.IntegrationProjectBackend.models.*;
import com.example.IntegrationProjectBackend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationScheduler {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private GeneratedScheduleRepository generatedScheduleRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private EmailService emailService;

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
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE"));
            LocalDateTime now = LocalDateTime.now();

            for (Student student : allStudents) {
                List<GeneratedSchedule> todaysTasks = generatedScheduleRepository
                        .findByStudentAndDayAndCompleted(student, today, false);

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
                                    String subject = task.getSubject() != null ? task.getSubject().getName() : "your subject";
                                    String topic = task.getTopic() != null ? task.getTopic() : "";
                                    
                                    sendGridEmailService.sendRevisionEndingReminder(
                                        student.getEmail(),
                                        studentName,
                                        subject,
                                        topic,
                                        (int) minutesUntilEnd
                                    );
                                    
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

    /**
     * Runs every day at 11:00 PM to check if students marked homework as finished
     * Sends to parent if available, otherwise sends to student directly
     */
    @Scheduled(cron = "0 0 23 * * *")
    public void checkUnfinishedHomework() {
        System.out.println("🔔 [" + LocalDateTime.now() + "] Checking for unfinished homework...");

        try {
            List<Student> allStudents = studentRepository.findAll();
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE"));

            for (Student student : allStudents) {
                List<GeneratedSchedule> unfinishedTasks = generatedScheduleRepository
                        .findByStudentAndDayAndCompleted(student, today, false);

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
                
                // Try to send to parent first, otherwise send to student
                Parent parent = student.getParent();
                if (parent != null && parent.getEmail() != null && !parent.getEmail().isEmpty()) {
                    // Send to parent
                    String parentName = parent.getFirstName() + " " + parent.getLastName();
                    sendGridEmailService.sendHomeworkAlert(
                            parent.getEmail(),
                            parentName,
                            studentName,
                            unfinishedTasks.size(),
                            taskList.toString());
                    System.out.println("⚠️ Sent homework alert to parent of " + studentName +
                            " (" + unfinishedTasks.size() + " tasks not completed)");
                } else {
                    // No parent or parent email unavailable - send to student
                    sendGridEmailService.sendHomeworkAlert(
                            student.getEmail(),
                            studentName,
                            studentName,
                            unfinishedTasks.size(),
                            taskList.toString());
                    System.out.println("⚠️ Sent homework alert directly to student " + studentName +
                            " (" + unfinishedTasks.size() + " tasks not completed)");
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
                            100.0
                    );
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
                            100.0
                    );
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
