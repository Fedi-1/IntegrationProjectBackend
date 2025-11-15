package com.example.IntegrationProjectBackend.controllers;

import com.example.IntegrationProjectBackend.dtos.ScheduleGenerationRequest;
import com.example.IntegrationProjectBackend.dtos.ScheduleGenerationResponse;
import com.example.IntegrationProjectBackend.services.MLServiceHealthCheckService;
import com.example.IntegrationProjectBackend.services.ScheduleGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class ScheduleGeneratorController {

    @Autowired
    private ScheduleGeneratorService scheduleGeneratorService;

    @Autowired
    private MLServiceHealthCheckService mlServiceHealthCheckService;

    /**
     * Generate a new schedule for a student using AI
     * POST /api/schedule/generate/{studentCin}
     */
    @PostMapping("/generate/{studentCin}")
    public ResponseEntity<ScheduleGenerationResponse> generateSchedule(
            @PathVariable String studentCin,
            @RequestBody ScheduleGenerationRequest request) {
        try {
            ScheduleGenerationResponse response = scheduleGeneratorService.generateSchedule(studentCin, request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            ScheduleGenerationResponse errorResponse = new ScheduleGenerationResponse(
                    null,
                    "Error generating schedule: " + e.getMessage(),
                    false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Generate schedule from uploaded PDF timetable
     * POST /api/schedule/generate-from-pdf/{studentCin}
     * Uses AI to extract subjects from PDF and generate schedule
     */
    @PostMapping("/generate-from-pdf/{studentCin}")
    public ResponseEntity<ScheduleGenerationResponse> generateFromPdf(
            @PathVariable String studentCin,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "maxStudyDuration", required = false, defaultValue = "30") Integer maxStudyDuration) {
        try {
            ScheduleGenerationResponse response = scheduleGeneratorService.generateScheduleFromPdf(
                    studentCin, file, maxStudyDuration);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                // Return 400 Bad Request if PDF processing failed
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            ScheduleGenerationResponse errorResponse = new ScheduleGenerationResponse(
                    null,
                    "Error generating schedule from PDF: " + e.getMessage(),
                    false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get the current saved schedule for a student
     * GET /api/schedule/student/{studentCin}
     */
    @GetMapping("/student/{studentCin}")
    public ResponseEntity<?> getStudentSchedule(@PathVariable String studentCin) {
        try {
            Map<String, Map<String, ScheduleGenerationResponse.ActivityBlock>> schedule = scheduleGeneratorService
                    .getStudentSchedule(studentCin);

            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a student's generated schedule
     * DELETE /api/schedule/student/{studentCin}
     */
    @DeleteMapping("/student/{studentCin}")
    public ResponseEntity<?> deleteStudentSchedule(@PathVariable String studentCin) {
        try {
            scheduleGeneratorService.deleteStudentSchedule(studentCin);
            return ResponseEntity.ok(Map.of("message", "Schedule deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     * GET /api/schedule/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Schedule Generator API",
                "mlServiceUrl", mlServiceHealthCheckService.getMLServiceUrl(),
                "mlServiceAvailable", mlServiceHealthCheckService.isMLServiceAvailable()));
    }
}
