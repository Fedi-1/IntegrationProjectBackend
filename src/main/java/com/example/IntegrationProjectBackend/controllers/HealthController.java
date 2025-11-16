package com.example.IntegrationProjectBackend.controllers;

import com.example.IntegrationProjectBackend.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
     * Usage: GET /api/test-email?to=your-email@example.com
     * This endpoint helps verify that email configuration is working correctly
     */
    @GetMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail(
            @RequestParam(required = false) String to) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Use provided email or default to a test address
            String recipientEmail = (to != null && !to.isEmpty()) ? to : "test@example.com";
            
            // Send test email
            emailService.sendEmail(
                recipientEmail,
                "✅ Test Email from IntegrationProject Backend",
                "Hello!\n\n" +
                "This is a test email to verify that your email configuration is working correctly.\n\n" +
                "✅ Email service is configured properly\n" +
                "✅ SMTP connection successful\n" +
                "✅ Email was sent at: " + LocalDateTime.now() + "\n\n" +
                "If you receive this email, your notification system is ready to send:\n" +
                "- Student homework reminders (10 PM daily)\n" +
                "- Parent revision alerts (11 PM daily)\n" +
                "- Quiz score notifications (hourly)\n\n" +
                "Best regards,\n" +
                "Your Study Management System"
            );
            
            response.put("success", true);
            response.put("message", "Test email sent successfully!");
            response.put("recipient", recipientEmail);
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("note", "Check your inbox (and spam folder) for the test email");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send email");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("troubleshooting", "Check that EMAIL_USERNAME and EMAIL_APP_PASSWORD are set in Render environment variables");
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
