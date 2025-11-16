package com.example.IntegrationProjectBackend.controllers;

import com.example.IntegrationProjectBackend.services.EmailService;
import com.example.IntegrationProjectBackend.services.NotificationScheduler;
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

    @Autowired
    private NotificationScheduler notificationScheduler;

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
                "\n\nIf you receive this, the email system is working correctly!"
            );
            
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
}
