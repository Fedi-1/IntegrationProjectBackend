package com.example.IntegrationProjectBackend.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Email service using SendGrid API (works on Render free tier)
 * SendGrid doesn't use SMTP ports, so it bypasses Render's restrictions
 */
@Service
public class SendGridEmailService {

    @Value("${sendgrid.api.key:NOT_SET}")
    private String sendGridApiKey;

    @Value("${spring.mail.username:louhichioussama59@gmail.com}")
    private String fromEmail;

    public boolean isConfigured() {
        return sendGridApiKey != null && !sendGridApiKey.equals("NOT_SET") && !sendGridApiKey.isEmpty();
    }

    /**
     * Send email using SendGrid API
     */
    public boolean sendEmail(String toEmail, String subject, String body) {
        if (!isConfigured()) {
            System.err.println("❌ SendGrid not configured. Set SENDGRID_API_KEY environment variable.");
            return false;
        }

        try {
            Email from = new Email(fromEmail);
            Email to = new Email(toEmail);
            Content content = new Content("text/plain", body);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println(
                        "✅ SendGrid email sent to: " + toEmail + " (Status: " + response.getStatusCode() + ")");
                return true;
            } else {
                System.err.println("❌ SendGrid error: " + response.getStatusCode() + " - " + response.getBody());
                return false;
            }

        } catch (IOException e) {
            System.err.println("❌ Failed to send email via SendGrid: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send revision ending reminder to student
     */
    public void sendRevisionEndingReminder(String toEmail, String studentName, String subject, String topic,
            int minutesLeft) {
        String topicText = (topic != null && !topic.isEmpty()) ? " on " + topic : "";
        String body = String.format(
                "Hello %s,\n\n" +
                        "⏰ Your revision session for %s%s is ending in %d minutes!\n\n" +
                        "Make sure to:\n" +
                        "✅ Complete your current topic\n" +
                        "✅ Review key points\n" +
                        "✅ Mark your session as finished when done\n\n" +
                        "Keep up the great work! 📚\n\n" +
                        "Best regards,\n" +
                        "Your Study Management System",
                studentName, subject, topicText, minutesLeft);

        sendEmail(toEmail, "⏰ Revision Session Ending Soon - " + subject, body);
    }

    /**
     * Send homework alert (to parent or student)
     */
    public void sendHomeworkAlert(String toEmail, String recipientName, String studentName,
            int unfinishedCount, String taskList) {
        String body = String.format(
                "Dear %s,\n\n" +
                        "⚠️ %s has %d task(s) that were not marked as finished today:\n\n" +
                        "%s\n" +
                        "Please remember to:\n" +
                        "• Complete all assigned tasks\n" +
                        "• Mark tasks as finished when done\n" +
                        "• Follow the revision schedule\n\n" +
                        "Regular completion tracking helps maintain good study habits.\n\n" +
                        "Best regards,\n" +
                        "Your Study Management System",
                recipientName, studentName, unfinishedCount, taskList);

        sendEmail(toEmail, "⚠️ Homework Not Completed - " + studentName, body);
    }

    /**
     * Send quiz score alert (to parent or student)
     */
    public void sendQuizAlert(String toEmail, String recipientName, String studentName, String quizTitle,
            double score, double maxScore) {
        double percentage = (score / maxScore) * 100;
        String emoji = percentage >= 70 ? "✅" : "⚠️";

        String body = String.format(
                "Dear %s,\n\n" +
                        "%s %s has completed a quiz:\n\n" +
                        "Quiz: %s\n" +
                        "Score: %.1f / %.1f (%.1f%%)\n\n" +
                        "%s\n\n" +
                        "Best regards,\n" +
                        "Your Study Management System",
                recipientName, emoji, studentName, quizTitle, score, maxScore, percentage,
                percentage >= 70 ? "Great job! Keep up the good work!"
                        : "Please review the material and practice more.");

        sendEmail(toEmail, "📝 Quiz Score Notification - " + studentName, body);
    }
}
