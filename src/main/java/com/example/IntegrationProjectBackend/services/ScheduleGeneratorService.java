package com.example.IntegrationProjectBackend.services;

import com.example.IntegrationProjectBackend.dtos.ScheduleGenerationResponse;
import com.example.IntegrationProjectBackend.models.GeneratedSchedule;
import com.example.IntegrationProjectBackend.models.Student;
import com.example.IntegrationProjectBackend.models.Subjects;
import com.example.IntegrationProjectBackend.repositories.GeneratedScheduleRepository;
import com.example.IntegrationProjectBackend.repositories.StudentRepository;
import com.example.IntegrationProjectBackend.repositories.SubjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class ScheduleGeneratorService {

    @Autowired
    private GeneratedScheduleRepository generatedScheduleRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SubjectsRepository subjectsRepository;

    @Autowired
    private AIScheduleService aiScheduleService; // Spring AI for Groq

    /**
     * Save generated schedule to database
     */
    private void saveGeneratedSchedule(Student student,
            Map<String, Map<String, Map<String, Object>>> scheduleData,
            String sessionId) {
        for (Map.Entry<String, Map<String, Map<String, Object>>> dayEntry : scheduleData.entrySet()) {
            String day = dayEntry.getKey();
            Map<String, Map<String, Object>> timeSlots = dayEntry.getValue();

            for (Map.Entry<String, Map<String, Object>> slotEntry : timeSlots.entrySet()) {
                String timeSlot = slotEntry.getKey();
                Map<String, Object> activityData = slotEntry.getValue();

                String activity = (String) activityData.get("activity");

                // Try to get duration from multiple possible field names
                Integer durationMinutes = null;
                if (activityData.containsKey("duration_minutes")) {
                    durationMinutes = (Integer) activityData.get("duration_minutes");
                } else if (activityData.containsKey("duration")) {
                    Object durationObj = activityData.get("duration");
                    durationMinutes = durationObj instanceof Integer ? (Integer) durationObj : null;
                }

                // If still null, calculate from time slot (e.g., "18:00-18:50" = 50 minutes)
                if (durationMinutes == null && timeSlot != null && timeSlot.contains("-")) {
                    durationMinutes = calculateDurationFromTimeSlot(timeSlot);
                }

                String subjectName = (String) activityData.get("subject");
                String topic = (String) activityData.get("topic");

                // Find or create subject
                Subjects subject = null;
                if (subjectName != null && !activity.equals("break")) {
                    subject = subjectsRepository.findByName(subjectName)
                            .orElseGet(() -> {
                                Subjects newSubject = new Subjects(subjectName);
                                return subjectsRepository.save(newSubject);
                            });
                }

                // Create and save generated schedule entry
                GeneratedSchedule generatedSchedule = new GeneratedSchedule(
                        student, day, timeSlot, activity, durationMinutes, subject, topic, sessionId);
                generatedScheduleRepository.save(generatedSchedule);
            }
        }
    }

    /**
     * Convert database format to response DTO format
     */
    private Map<String, Map<String, ScheduleGenerationResponse.ActivityBlock>> convertToResponseFormat(
            Map<String, Map<String, Map<String, Object>>> scheduleData) {

        Map<String, Map<String, ScheduleGenerationResponse.ActivityBlock>> result = new HashMap<>();

        for (Map.Entry<String, Map<String, Map<String, Object>>> dayEntry : scheduleData.entrySet()) {
            String day = dayEntry.getKey();
            Map<String, Map<String, Object>> timeSlots = dayEntry.getValue();
            Map<String, ScheduleGenerationResponse.ActivityBlock> daySchedule = new HashMap<>();

            for (Map.Entry<String, Map<String, Object>> slotEntry : timeSlots.entrySet()) {
                String timeSlot = slotEntry.getKey();
                Map<String, Object> activityData = slotEntry.getValue();

                ScheduleGenerationResponse.ActivityBlock block = new ScheduleGenerationResponse.ActivityBlock();
                block.setActivity((String) activityData.get("activity"));

                // Handle both "duration" and "duration_minutes" from AI
                Integer durationMinutes = (Integer) activityData.get("duration_minutes");
                if (durationMinutes == null && activityData.containsKey("duration")) {
                    Object durationObj = activityData.get("duration");
                    durationMinutes = durationObj instanceof Integer ? (Integer) durationObj : null;
                }
                block.setDurationMinutes(durationMinutes);

                block.setSubject((String) activityData.get("subject"));
                block.setTopic((String) activityData.get("topic"));

                daySchedule.put(timeSlot, block);
            }

            result.put(day, daySchedule);
        }

        return result;
    }

    /**
     * Get saved schedule for a student
     */
    public Map<String, Map<String, ScheduleGenerationResponse.ActivityBlock>> getStudentSchedule(String studentCin) {
        Student student = studentRepository.findByCin(studentCin)
                .orElseThrow(() -> new RuntimeException("Student not found with CIN: " + studentCin));

        List<GeneratedSchedule> schedules = generatedScheduleRepository.findByStudentOrderByDayAscTimeSlotAsc(student);

        Map<String, Map<String, ScheduleGenerationResponse.ActivityBlock>> result = new HashMap<>();

        for (GeneratedSchedule schedule : schedules) {
            String day = schedule.getDay();
            result.putIfAbsent(day, new HashMap<>());

            ScheduleGenerationResponse.ActivityBlock block = new ScheduleGenerationResponse.ActivityBlock();
            block.setActivity(schedule.getActivity());
            block.setDurationMinutes(schedule.getDurationMinutes());
            block.setSubject(schedule.getSubject() != null ? schedule.getSubject().getName() : null);
            block.setTopic(schedule.getTopic());

            result.get(day).put(schedule.getTimeSlot(), block);
        }

        return result;
    }

    /**
     * Delete student's generated schedule
     */
    @Transactional
    public void deleteStudentSchedule(String studentCin) {
        Student student = studentRepository.findByCin(studentCin)
                .orElseThrow(() -> new RuntimeException("Student not found with CIN: " + studentCin));

        generatedScheduleRepository.deleteByStudent(student);
    }

    /**
     * Generate schedule from uploaded PDF timetable
     * Uses Spring AI to extract text from PDF and analyze it
     */
    @Transactional
    public ScheduleGenerationResponse generateScheduleFromPdf(String studentCin, MultipartFile file,
            Integer maxStudyDuration) {
        try {
            System.out.println("[ScheduleGenerator] Starting PDF schedule generation for student: " + studentCin);

            // 1. Find student
            Student student = studentRepository.findByCin(studentCin)
                    .orElseThrow(() -> new RuntimeException("Student not found with CIN: " + studentCin));

            // 2. Extract text from PDF using AI
            System.out.println("[ScheduleGenerator] Extracting PDF content...");
            String pdfContent = extractPdfContent(file);

            if (pdfContent == null || pdfContent.trim().isEmpty()) {
                return new ScheduleGenerationResponse(
                        null,
                        "Could not extract text from PDF. Please ensure the PDF contains readable text.",
                        false);
            }

            System.out.println("[ScheduleGenerator] PDF content extracted: "
                    + pdfContent.substring(0, Math.min(200, pdfContent.length())) + "...");

            // 3. Use AI to analyze the PDF content and extract subjects
            System.out.println("[ScheduleGenerator] Analyzing PDF with AI...");
            List<Map<String, Object>> extractedSubjects = aiScheduleService.extractSubjectsFromText(pdfContent);

            if (extractedSubjects == null || extractedSubjects.isEmpty()) {
                return new ScheduleGenerationResponse(
                        null,
                        "Could not identify subjects from PDF. Please use manual schedule generation.",
                        false);
            }

            // 4. Extract school end times from PDF
            System.out.println("[ScheduleGenerator] Extracting school schedule times...");
            Map<String, String> schoolEndTimes = aiScheduleService.extractSchoolEndTimes(pdfContent);

            // 5. Get student's preparation time (default to 30 minutes if not set)
            int preparationTime = student.getPreparationTimeMinutes() != null
                    ? student.getPreparationTimeMinutes()
                    : 30;
            System.out.println("[ScheduleGenerator] Student preparation time: " + preparationTime + " minutes");

            // 6. Prepare preferences
            Map<String, Object> preferences = new HashMap<>();
            preferences.put("dailyStudyHours", maxStudyDuration);
            preferences.put("preferredTime", "evening");
            preferences.put("preparationTime", preparationTime);

            // 7. Generate DYNAMIC schedule using school times + preparation time
            System.out.println("[ScheduleGenerator] Generating DYNAMIC schedule with school-based start times...");
            Object aiResponse = aiScheduleService.generateScheduleWithSchoolTimes(
                    extractedSubjects,
                    maxStudyDuration,
                    preferences,
                    schoolEndTimes,
                    preparationTime);

            // 8. Parse AI response with type safety - CHECK TYPE FIRST!
            System.out.println("[ScheduleGenerator] DEBUG: aiResponse type = " +
                    (aiResponse != null ? aiResponse.getClass().getName() : "null"));

            Map<String, Map<String, Map<String, Object>>> scheduleData;

            // CRITICAL FIX: AI might return List directly OR Map with "schedule" field
            if (aiResponse instanceof List) {
                // AI returned List directly: [{day: "Monday", timeSlot: "16:00-16:50", ...},
                // ...]
                System.out.println("[ScheduleGenerator] AI returned List directly");
                System.out.println("[ScheduleGenerator] Converting " + ((List<?>) aiResponse).size()
                        + " schedule items from List to Map format");
                scheduleData = convertListToScheduleMap((List<Map<String, Object>>) aiResponse);
                System.out.println("[ScheduleGenerator] Converted schedule covers " + scheduleData.size() + " days");
            } else if (aiResponse instanceof Map) {
                // AI returned Map - check if it has "schedule" field
                @SuppressWarnings("unchecked")
                Map<String, Object> aiResponseMap = (Map<String, Object>) aiResponse;
                Object scheduleField = aiResponseMap.get("schedule");

                System.out.println("[ScheduleGenerator] DEBUG: scheduleField type = " +
                        (scheduleField != null ? scheduleField.getClass().getName() : "null"));

                if (scheduleField instanceof List) {
                    // Convert List of schedule items to Map structure
                    scheduleData = convertListToScheduleMap((List<Map<String, Object>>) scheduleField);
                } else if (scheduleField instanceof Map) {
                    // Already in Map format
                    scheduleData = (Map<String, Map<String, Map<String, Object>>>) scheduleField;
                } else if (aiResponseMap.containsKey("Monday")) {
                    // Legacy format: entire response is the schedule
                    scheduleData = (Map<String, Map<String, Map<String, Object>>>) aiResponse;
                } else {
                    System.err.println("[ScheduleGenerator] ERROR: Unexpected AI response structure");
                    System.err.println("[ScheduleGenerator] Response type: "
                            + (scheduleField != null ? scheduleField.getClass() : "null"));
                    return new ScheduleGenerationResponse(
                            null,
                            "AI returned unexpected schedule format",
                            false);
                }
            } else {
                // Unknown type
                System.err.println("[ScheduleGenerator] ERROR: aiResponse is not List or Map");
                System.err
                        .println("[ScheduleGenerator] Type: " + (aiResponse != null ? aiResponse.getClass() : "null"));
                return new ScheduleGenerationResponse(
                        null,
                        "AI returned unexpected response type",
                        false);
            }

            // 9. Delete old schedules for this student
            generatedScheduleRepository.deleteByStudent(student);

            // 10. Generate unique session ID
            String sessionId = UUID.randomUUID().toString();

            // 11. Save to database
            saveGeneratedSchedule(student, scheduleData, sessionId);

            // 12. Prepare response
            Map<String, Map<String, ScheduleGenerationResponse.ActivityBlock>> responseSchedule = convertToResponseFormat(
                    scheduleData);

            System.out.println("[ScheduleGenerator] ✓ DYNAMIC PDF schedule saved successfully");
            System.out.println(
                    "[ScheduleGenerator] ✓ Schedule adapts to school times + " + preparationTime + "min prep time");
            return new ScheduleGenerationResponse(
                    responseSchedule,
                    "Smart revision schedule generated! Start times adapt to your school schedule + preparation time.",
                    true);

        } catch (Exception e) {
            System.err.println("[ScheduleGenerator] PDF Error: " + e.getMessage());
            e.printStackTrace();
            return new ScheduleGenerationResponse(
                    null,
                    "Error processing PDF: " + e.getMessage(),
                    false);
        }
    }

    /**
     * Extract text content from PDF file
     * Uses Apache PDFBox library
     */
    private String extractPdfContent(MultipartFile file) {
        try {
            // Use Apache PDFBox to extract text (PDFBox 3.x API - static method)
            org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(file.getBytes());
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return text;
        } catch (Exception e) {
            System.err.println("[ScheduleGenerator] PDF extraction error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calculate duration in minutes from time slot string (e.g., "18:00-18:50" =
     * 50)
     */
    private Integer calculateDurationFromTimeSlot(String timeSlot) {
        try {
            String[] parts = timeSlot.split("-");
            if (parts.length != 2)
                return null;

            String[] startParts = parts[0].trim().split(":");
            String[] endParts = parts[1].trim().split(":");

            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);

            int startTotalMinutes = startHour * 60 + startMinute;
            int endTotalMinutes = endHour * 60 + endMinute;

            return endTotalMinutes - startTotalMinutes;
        } catch (Exception e) {
            System.err.println("[ScheduleGenerator] Error calculating duration from time slot: " + timeSlot);
            return null;
        }
    }

    /**
     * Convert AI's List-based schedule format to Map-based format expected by the
     * rest of the system
     * AI returns: [{"day": "Monday", "timeSlot": "14:00-14:30", "subject": "Math",
     * ...}, ...]
     * System expects: {"Monday": {"14:00-14:30": {"subject": "Math", ...}}, ...}
     */
    private Map<String, Map<String, Map<String, Object>>> convertListToScheduleMap(
            List<Map<String, Object>> scheduleList) {
        Map<String, Map<String, Map<String, Object>>> scheduleMap = new HashMap<>();

        System.out.println(
                "[ScheduleGenerator] Converting " + scheduleList.size() + " schedule items from List to Map format");

        for (Map<String, Object> item : scheduleList) {
            String day = (String) item.get("day");
            String timeSlot = (String) item.get("timeSlot");

            if (day == null || timeSlot == null) {
                System.err.println("[ScheduleGenerator] WARNING: Skipping item with null day or timeSlot: " + item);
                continue;
            }

            // Ensure day map exists
            scheduleMap.putIfAbsent(day, new HashMap<>());

            // Create activity data (everything except day and timeSlot)
            Map<String, Object> activityData = new HashMap<>(item);
            activityData.remove("day");
            activityData.remove("timeSlot");

            // Add to schedule structure
            scheduleMap.get(day).put(timeSlot, activityData);
        }

        System.out.println("[ScheduleGenerator] Converted schedule covers " + scheduleMap.size() + " days");
        return scheduleMap;
    }
}
