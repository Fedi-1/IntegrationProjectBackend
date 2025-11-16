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
     * Send reminder to student when revision session is about to end
     */
    public void sendRevisionEndingReminder(String toEmail, String studentName, String subject, String topic,
            int minutesLeft) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("⏰ Revision Session Ending Soon - " + subject);

            String topicText = (topic != null && !topic.isEmpty()) ? " on " + topic : "";

            message.setText(String.format(
                    "Hello %s,\n\n" +
                            "⏰ Your revision session for %s%s is ending in %d minutes!\n\n" +
                            "Make sure to:\n" +
                            "✅ Complete your current topic\n" +
                            "✅ Review key points\n" +
                            "✅ Mark your session as finished when done\n\n" +
                            "Keep up the great work! 📚\n\n" +
                            "Best regards,\n" +
                            "Your Study Management System",
                    studentName, subject, topicText, minutesLeft));

            mailSender.send(message);
            System.out.println("✅ Revision ending reminder sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send reminder to " + toEmail + ": " + e.getMessage());
        }
    }

    /**
     * Send alert to parent when student didn't mark homework as finished
     */
    public void sendParentHomeworkAlert(String toEmail, String parentName, String studentName,
            int unfinishedCount, String taskList) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("⚠️ Homework Not Completed - " + studentName);
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "⚠️ %s has %d task(s) that were not marked as finished today:\n\n" +
                            "%s\n" +
                            "Please remind your child to:\n" +
                            "• Complete all assigned tasks\n" +
                            "• Mark tasks as finished when done\n" +
                            "• Follow the revision schedule\n\n" +
                            "Regular completion tracking helps maintain good study habits.\n\n" +
                            "Best regards,\n" +
                            "Your Study Management System",
                    parentName, studentName, unfinishedCount, taskList));

            mailSender.send(message);
            System.out.println("✅ Homework alert sent to parent: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send parent alert to " + toEmail + ": " + e.getMessage());
        }
    }

    /**
     * Send quiz score alert to parent
     */
    public void sendParentQuizAlert(String toEmail, String parentName, String studentName, String quizTitle,
            double score, double maxScore) {
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
                    percentage >= 70 ? "Great job! Keep up the good work!"
                            : "Please review the material and provide additional support."));

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
