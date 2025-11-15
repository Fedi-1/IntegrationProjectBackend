package com.example.IntegrationProjectBackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send a homework reminder to student
     */
    public void sendStudentReminder(String toEmail, String studentName, String taskDescription) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("⏰ Homework Reminder - Don't Forget!");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "This is a friendly reminder that you have pending homework:\n\n" +
                "📚 Task: %s\n\n" +
                "Please complete it before the deadline.\n\n" +
                "Good luck with your studies!\n\n" +
                "Best regards,\n" +
                "Your Study Management System",
                studentName, taskDescription
            ));
            
            mailSender.send(message);
            System.out.println("✅ Reminder email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }

    /**
     * Send revision adherence alert to parent
     */
    public void sendParentRevisionAlert(String toEmail, String parentName, String studentName, int completionPercentage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("⚠️ Revision Schedule Alert - " + studentName);
            message.setText(String.format(
                "Dear %s,\n\n" +
                "We noticed that %s is not following the revision schedule properly.\n\n" +
                "📊 Current Completion Rate: %d%%\n\n" +
                "Please encourage your child to stay on track with their study plan.\n\n" +
                "Best regards,\n" +
                "Your Study Management System",
                parentName, studentName, completionPercentage
            ));
            
            mailSender.send(message);
            System.out.println("✅ Revision alert sent to parent: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send parent alert to " + toEmail + ": " + e.getMessage());
        }
    }

    /**
     * Send quiz score alert to parent
     */
    public void sendParentQuizAlert(String toEmail, String parentName, String studentName, String quizTitle, double score, double maxScore) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("📝 Quiz Score Notification - " + studentName);
            
            double percentage = (score / maxScore) * 100;
            String emoji = percentage >= 70 ? "✅" : "⚠️";
            
            message.setText(String.format(
                "Dear %s,\n\n" +
                "%s %s has completed a quiz:\n\n" +
                "Quiz: %s\n" +
                "Score: %.1f / %.1f (%.1f%%)\n\n" +
                "%s\n\n" +
                "Best regards,\n" +
                "Your Study Management System",
                parentName, emoji, studentName, quizTitle, score, maxScore, percentage,
                percentage >= 70 ? "Great job! Keep up the good work!" : "Please review the material and provide additional support."
            ));
            
            mailSender.send(message);
            System.out.println("✅ Quiz alert sent to parent: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send quiz alert to " + toEmail + ": " + e.getMessage());
        }
    }

    /**
     * Generic method to send any email
     */
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            System.out.println("✅ Email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }
}
