package com.example.IntegrationProjectBackend.controllers;

import com.example.IntegrationProjectBackend.services.EmailService;
import com.example.IntegrationProjectBackend.services.NotificationScheduler;
import com.example.IntegrationProjectBackend.services.SendGridEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationScheduler notificationScheduler;

    @Autowired
    private SendGridEmailService sendGridEmailService;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:NOT_SET}")
    private String emailUsername;

    @Value("${spring.mail.host:NOT_SET}")
    private String mailHost;

    @Value("${spring.mail.port:NOT_SET}")
    private String mailPort;

    @Value("${sendgrid.api.key:NOT_SET}")
    private String sendGridApiKey;

    /**
     * Health check endpoint for monitoring services like UptimeRobot
     * This keeps the Render service awake by receiving regular pings
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "IntegrationProjectBackend");
        response.put("message", "Service is running");

        return ResponseEntity.ok(response);
    }

    /**
     * Alternative ping endpoint (simpler)
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    /**
     * Test email sending functionality
     */
    @GetMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam(required = false) String to) {
        Map<String, Object> response = new HashMap<>();

        try {
            String recipient = (to != null && !to.isEmpty()) ? to : "feditriki05@gmail.com";

            emailService.sendEmail(
                    recipient,
                    "Test Email - IntegrationProject",
                    "This is a test email sent at " + LocalDateTime.now() +
                            "\n\nIf you receive this, the email system is working correctly!");

            response.put("success", true);
            response.put("message", "Test email sent successfully");
            response.put("recipient", recipient);
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Check email configuration and SMTP settings
     */
    @GetMapping("/email-config")
    public ResponseEntity<Map<String, Object>> checkEmailConfig() {
        Map<String, Object> response = new HashMap<>();

        response.put("emailUsername", emailUsername);
        response.put("mailHost", mailHost);
        response.put("mailPort", mailPort);
        response.put("mailSenderConfigured", mailSender != null);
        response.put("sendgrid_configured", sendGridEmailService.isConfigured());
        response.put("sendgrid_api_key_set", !sendGridApiKey.equals("NOT_SET") && !sendGridApiKey.isEmpty());

        // Check if credentials are set
        boolean isConfigured = !emailUsername.equals("NOT_SET") &&
                !mailHost.equals("NOT_SET");

        response.put("isFullyConfigured", isConfigured);

        if (!isConfigured) {
            response.put("warning",
                    "Email not fully configured. Check environment variables: EMAIL_USERNAME, EMAIL_APP_PASSWORD");
        }

        response.put("instructions", Map.of(
                "step1", "Ensure EMAIL_USERNAME is set to your Gmail address",
                "step2", "Ensure EMAIL_APP_PASSWORD is set to your Gmail App Password (not regular password)",
                "step3", "Gmail must have 2-Factor Authentication enabled",
                "step4", "Check spam folder in Gmail",
                "step5", "May take 1-2 minutes for email to arrive"));

        return ResponseEntity.ok(response);
    }

    /**
     * Enhanced email test with more details
     */
    @GetMapping("/test-email-detailed")
    public ResponseEntity<Map<String, Object>> testEmailDetailed(@RequestParam(required = false) String to) {
        Map<String, Object> response = new HashMap<>();

        try {
            String recipient = (to != null && !to.isEmpty()) ? to : "feditriki05@gmail.com";

            // Check configuration first
            if (emailUsername.equals("NOT_SET")) {
                response.put("success", false);
                response.put("error", "EMAIL_USERNAME not configured in environment variables");
                return ResponseEntity.status(500).body(response);
            }

            // Send test email
            String testTime = LocalDateTime.now().toString();
            emailService.sendEmail(
                    recipient,
                    "🔔 Test Email - " + testTime,
                    "✅ SUCCESS! Email system is working!\n\n" +
                            "📧 From: " + emailUsername + "\n" +
                            "📧 To: " + recipient + "\n" +
                            "⏰ Sent at: " + testTime + "\n\n" +
                            "If you can read this, your notification system should work.\n\n" +
                            "⚠️ CHECK YOUR SPAM FOLDER if you don't see this in inbox!\n\n" +
                            "Next steps:\n" +
                            "1. Check your spam/junk folder\n" +
                            "2. Mark as 'Not Spam' if found there\n" +
                            "3. Add sender to contacts\n" +
                            "4. Wait 1-2 minutes for delivery\n\n" +
                            "Best regards,\n" +
                            "IntegrationProject Notification System");

            response.put("success", true);
            response.put("message", "Test email sent successfully");
            response.put("recipient", recipient);
            response.put("sender", emailUsername);
            response.put("timestamp", testTime);
            response.put("important", "CHECK YOUR SPAM FOLDER! Emails from new senders often go there first.");
            response.put("waitTime", "Allow 1-2 minutes for email delivery");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            response.put("suggestion",
                    "Check Render logs for detailed error. Verify EMAIL_USERNAME and EMAIL_APP_PASSWORD are set correctly.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Manually trigger notification checks for testing
     */
    @GetMapping("/trigger-notifications")
    public ResponseEntity<Map<String, Object>> triggerNotifications(@RequestParam String type) {
        Map<String, Object> response = new HashMap<>();

        try {
            switch (type.toLowerCase()) {
                case "revision":
                    notificationScheduler.checkUpcomingRevisionSessions();
                    response.put("message", "Revision session check triggered");
                    break;
                case "homework":
                    notificationScheduler.checkUnfinishedHomework();
                    response.put("message", "Unfinished homework check triggered");
                    break;
                case "quiz":
                    notificationScheduler.checkQuizScores();
                    response.put("message", "Quiz score check triggered");
                    break;
                case "all":
                    notificationScheduler.checkUpcomingRevisionSessions();
                    notificationScheduler.checkUnfinishedHomework();
                    notificationScheduler.checkQuizScores();
                    response.put("message", "All notification checks triggered");
                    break;
                default:
                    response.put("error", "Invalid type. Use: revision, homework, quiz, or all");
                    return ResponseEntity.badRequest().body(response);
            }

            response.put("success", true);
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test SendGrid email service
     */
    @GetMapping("/test-sendgrid")
    public ResponseEntity<Map<String, Object>> testSendGrid(@RequestParam(required = false) String to) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String recipient = (to != null && !to.isEmpty()) ? to : "feditriki05@gmail.com";

            if (!sendGridEmailService.isConfigured()) {
                response.put("success", false);
                response.put("error", "SendGrid is not configured. Please set SENDGRID_API_KEY environment variable.");
                response.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.ok(response);
            }

            boolean sent = sendGridEmailService.sendEmail(
                recipient,
                "Test Email from SendGrid",
                "This is a test email sent via SendGrid HTTP API at " + LocalDateTime.now() + 
                "\n\nIf you received this, the SendGrid integration is working correctly!" +
                "\n\nSendGrid bypasses SMTP port restrictions on Render free tier."
            );

            response.put("success", sent);
            response.put("recipient", recipient);
            response.put("service", "SendGrid HTTP API");
            response.put("timestamp", LocalDateTime.now().toString());
            
            if (sent) {
                response.put("message", "Email sent successfully via SendGrid!");
            } else {
                response.put("message", "Failed to send email via SendGrid. Check logs for details.");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
        }

        return ResponseEntity.ok(response);
    }
}
