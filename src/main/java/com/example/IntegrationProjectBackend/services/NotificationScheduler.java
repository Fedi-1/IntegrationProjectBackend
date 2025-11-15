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

    /**
     * Runs every day at 10:00 PM to check for incomplete homework/tasks
     * Cron format: second, minute, hour, day of month, month, day of week
     * "0 0 22 * * *" = At 10:00 PM every day
     */
    @Scheduled(cron = "0 0 22 * * *")
    public void checkIncompleteHomework() {
        System.out.println("🔔 [" + LocalDateTime.now() + "] Running homework reminder check...");
        
        try {
            List<Student> allStudents = studentRepository.findAll();
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE")); // e.g., "Monday"
            
            for (Student student : allStudents) {
                // Find today's incomplete tasks for this student
                List<GeneratedSchedule> incompleteTasks = generatedScheduleRepository
                    .findByStudentAndDayAndCompleted(student, today, false);
                
                if (!incompleteTasks.isEmpty()) {
                    // Build task description
                    StringBuilder taskDescription = new StringBuilder();
                    for (GeneratedSchedule task : incompleteTasks) {
                        taskDescription.append("- ")
                            .append(task.getTimeSlot())
                            .append(": ")
                            .append(task.getActivity());
                        if (task.getSubject() != null) {
                            taskDescription.append(" (").append(task.getSubject().getName()).append(")");
                        }
                        if (task.getTopic() != null) {
                            taskDescription.append(" - ").append(task.getTopic());
                        }
                        taskDescription.append("\n");
                    }
                    
                    // Send reminder email
                    String studentName = student.getFirstName() + " " + student.getLastName();
                    emailService.sendStudentReminder(
                        student.getEmail(),
                        studentName,
                        taskDescription.toString()
                    );
                    
                    System.out.println("✅ Sent reminder to " + studentName + " (" + incompleteTasks.size() + " tasks)");
                }
            }
            
            System.out.println("✅ Homework reminder check completed");
        } catch (Exception e) {
            System.err.println("❌ Error in homework reminder check: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Runs every day at 11:00 PM to check revision adherence for parents
     * "0 0 23 * * *" = At 11:00 PM every day
     */
    @Scheduled(cron = "0 0 23 * * *")
    public void checkRevisionAdherence() {
        System.out.println("🔔 [" + LocalDateTime.now() + "] Running revision adherence check for parents...");
        
        try {
            List<Student> allStudents = studentRepository.findAll();
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE"));
            
            for (Student student : allStudents) {
                Parent parent = student.getParent();
                if (parent == null || parent.getEmail() == null) {
                    continue; // Skip if no parent or parent has no email
                }
                
                // Get today's schedule for the student
                List<GeneratedSchedule> todaysSchedule = generatedScheduleRepository
                    .findByStudentAndDay(student, today);
                
                if (todaysSchedule.isEmpty()) {
                    continue; // No schedule for today
                }
                
                // Calculate completion percentage
                long totalTasks = todaysSchedule.size();
                long completedTasks = todaysSchedule.stream()
                    .filter(task -> task.getCompleted() != null && task.getCompleted())
                    .count();
                
                int completionPercentage = (int) ((completedTasks * 100) / totalTasks);
                
                // Alert parent if completion is below 50%
                if (completionPercentage < 50) {
                    String parentName = parent.getFirstName() + " " + parent.getLastName();
                    String studentName = student.getFirstName() + " " + student.getLastName();
                    
                    emailService.sendParentRevisionAlert(
                        parent.getEmail(),
                        parentName,
                        studentName,
                        completionPercentage
                    );
                    
                    System.out.println("⚠️ Sent revision alert to parent of " + studentName + 
                                     " (completion: " + completionPercentage + "%)");
                }
            }
            
            System.out.println("✅ Revision adherence check completed");
        } catch (Exception e) {
            System.err.println("❌ Error in revision adherence check: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check for poor quiz scores and notify parents
     * Runs every hour to catch newly completed quizzes
     * "0 0 * * * *" = Every hour on the hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkQuizScores() {
        System.out.println("🔔 [" + LocalDateTime.now() + "] Running quiz score check...");
        
        try {
            // Find recently completed quizzes (within last hour)
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            List<Quiz> recentQuizzes = quizRepository.findByStatusAndCompletedAtAfter("completed", oneHourAgo);
            
            for (Quiz quiz : recentQuizzes) {
                Student student = quiz.getStudent();
                Parent parent = student.getParent();
                
                if (parent == null || parent.getEmail() == null) {
                    continue; // Skip if no parent or parent has no email
                }
                
                Double score = quiz.getScore();
                if (score == null) {
                    continue; // Skip if score not calculated
                }
                
                // Notify parent for all quiz results (can filter by score threshold if needed)
                // Currently notifies for all quizzes, marks poor scores differently
                String parentName = parent.getFirstName() + " " + parent.getLastName();
                String studentName = student.getFirstName() + " " + student.getLastName();
                String quizTitle = quiz.getSubject() + " - " + quiz.getTopic();
                
                emailService.sendParentQuizAlert(
                    parent.getEmail(),
                    parentName,
                    studentName,
                    quizTitle,
                    score,
                    100.0 // Assuming score is percentage
                );
                
                String emoji = score >= 70 ? "✅" : "⚠️";
                System.out.println(emoji + " Sent quiz notification to parent of " + studentName + 
                                 " (score: " + score + "%)");
            }
            
            System.out.println("✅ Quiz score check completed");
        } catch (Exception e) {
            System.err.println("❌ Error in quiz score check: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test method - runs every minute for debugging (disable in production)
     * Uncomment only for testing
     */
    // @Scheduled(cron = "0 * * * * *")
    // public void testScheduler() {
    //     System.out.println("✅ Scheduler is working at: " + LocalDateTime.now());
    // }
}
