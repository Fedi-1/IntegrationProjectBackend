package com.example.IntegrationProjectBackend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class AIScheduleService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AIScheduleService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate a personalized study schedule using AI
     * 
     * @param subjects       List of subjects with their topics and difficulty
     * @param availableHours Study hours available per day
     * @param preferences    Student learning preferences
     * @return JSON object with generated schedule
     */
    public Object generateSchedule(List<Map<String, Object>> subjects, int availableHours,
            Map<String, Object> preferences) {
        System.out.println("[AIScheduleService] Starting schedule generation");

        String subjectsJson = convertSubjectsToJson(subjects);
        String preferencesJson = convertPreferencesToJson(preferences);

        String prompt = String.format(
                """
                        You are an expert educational planner. Generate a personalized weekly study schedule based on the following information:

                        SUBJECTS AND TOPICS:
                        %s

                        MAX STUDY DURATION: %d minutes per session (STRICT LIMIT - never exceed this!)

                        STUDENT PREFERENCES:
                        %s

                        CRITICAL SCHEDULING RULES:
                        1. **SUNDAY IS COMPLETELY FREE** - NO study sessions, NO breaks, NOTHING on Sunday
                        2. **School Hours (08:00-18:00 on weekdays)** - AVOID these hours, they are for classes
                        3. **Study ONLY in evening slots**: 18:00-22:00 on weekdays (Monday-Saturday)
                        4. **Weekend (Saturday)**: Can use morning (09:00-12:00) and afternoon (14:00-18:00) and evening (18:00-22:00)
                        5. **Each study session**: Maximum %d minutes (this is student's max_study_duration)
                        6. **Break duration**: 10-15 minutes between sessions
                        7. **Total daily study time**: Should not exceed 3-4 hours to avoid burnout

                        TIME SLOT GUIDELINES:
                        - Monday-Friday: ONLY 18:00-22:00 (after school)
                        - Saturday: 09:00-12:00, 14:00-18:00, 18:00-22:00 (no school)
                        - Sunday: COMPLETELY FREE (no entries at all)

                        STUDY SESSION STRUCTURE:
                        - Each session should be 30-50 minutes (but never exceed max_study_duration: %d minutes)
                        - Include 10-15 minute breaks between sessions
                        - Prioritize difficult subjects in early evening (18:00-20:00) when energy is higher
                        - Mix subjects throughout the week for variety
                        - Include review sessions for difficult topics

                        Respond ONLY with a JSON ARRAY (not wrapped in object), no markdown, no code blocks:

                        [
                          {
                            "day": "Monday",
                            "timeSlot": "18:00-18:30",
                            "subject": "Subject name",
                            "topic": "Specific topic",
                            "activity": "study",
                            "difficulty": "hard",
                            "duration": 30
                          },
                          {
                            "day": "Monday",
                            "timeSlot": "18:30-19:00",
                            "subject": null,
                            "topic": null,
                            "activity": "break",
                            "difficulty": null,
                            "duration": 15
                          }
                        ]

                        REMEMBER:
                        - NO entries for Sunday (leave it completely empty)
                        - Weekday study: 18:00-22:00 ONLY
                        - Respect max_study_duration: %d minutes per session
                        - Include "break" activities with activity="break" and subject=null
                        """,
                subjectsJson, availableHours, preferencesJson, availableHours, availableHours, availableHours);

        try {
            var response = chatClient.prompt()
                    .user(prompt)
                    .call();

            String jsonString = Objects.requireNonNull(response.content());
            System.out.println("[AIScheduleService] AI response received");

            String cleanJson = extractJsonFromResponse(jsonString);
            Object parsedJson = objectMapper.readValue(cleanJson, Object.class);

            System.out.println("[AIScheduleService] Schedule generated successfully");
            return parsedJson;

        } catch (Exception e) {
            System.err.println("[AIScheduleService] Error generating schedule: " + e.getMessage());
            throw new RuntimeException("Error generating schedule with AI: " + e.getMessage(), e);
        }
    }

    private String convertSubjectsToJson(List<Map<String, Object>> subjects) {
        try {
            return objectMapper.writeValueAsString(subjects);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String convertPreferencesToJson(Map<String, Object> preferences) {
        try {
            return objectMapper.writeValueAsString(preferences);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * Extract school schedule times from PDF timetable
     * Returns a map with day -> last class end time
     */
    public Map<String, String> extractSchoolEndTimes(String pdfText) {
        System.out.println("[AIScheduleService] Extracting school end times from PDF...");
        System.out.println("[AIScheduleService] PDF text length: " + pdfText.length() + " characters");

        String prompt = String.format(
                """
                        You are analyzing a French university timetable (emploi du temps). Extract when school ENDS each day.

                        EXAMPLE 1 - How to read course boxes in timetable:

                        🔑 KEY FORMAT: Each course appears in a BOX with this layout:
                        ```
                        ┌─────────────────────────┐
                        │ Course Name             │
                        │ Professor Name          │
                        │ Room Code               │
                        │ 08:15 ← START TIME      │ (top left)
                        │ 11:15 ← END TIME        │ (bottom left)
                        └─────────────────────────┘
                        ```

                        CRITICAL:
                        - **TOP LEFT corner** = START time of course
                        - **BOTTOM LEFT corner** = END time of course
                        - The END time is what you need to extract!

                        Example 1:
                        ```
                        Atelier Framework Côté Serveur
                        KHAYATI Alya
                        G1 LI2
                        15:00  ← START
                        18:00  ← END (this is the answer!)
                        ```
                        → This course ENDS at **18:00**

                        Example 2:
                        ```
                        Atelier Développement Mobile
                        BEN HADJ SAID Ramzi
                        G1 FC
                        08:15  ← START
                        11:15  ← END (this is the answer!)
                        ```
                        → This course ENDS at **11:15**

                        Example 3:
                        ```
                        Conception Orientée Objet
                        CHEBBI Ikram
                        I5
                        13:30  ← START
                        15:00  ← END (this is the answer!)
                        ```
                        → This course ENDS at **15:00**

                        EXAMPLE 2 - Analyzing a complete day with MULTIPLE classes:

                        Lundi (Monday) column shows:
                        - "Atelier Développement" 08:15-11:15 (ends 11:15)
                        - "Initiation intelligence" 11:30-13:00 (ends 13:00)
                        - "Atelier intelligence" 13:30-16:30 (ends 16:30) ← LATEST!
                        - END TIME: **16:30** (the MAXIMUM of 11:15, 13:00, 16:30)

                        Mardi (Tuesday) column shows:
                        - "Technique de Recherche" 08:15-09:45 (ends 09:45)
                        - "Conception Orientée" 10:00-11:30 (ends 11:30)
                        - "Conception Orientée" 11:30-13:00 (ends 13:00)
                        - "Développement Mobile" 13:30-15:00 (ends 15:00) ← LATEST!
                        - END TIME: **15:00** (the MAXIMUM of all end times)

                        Mercredi (Wednesday) column shows:
                        - "Développement Mobile" 08:15-09:45 (ends 09:45)
                        - "Atelier programmation" 11:30-13:00 (ends 13:00)
                        - "Atelier programmation" 13:30-15:00 (ends 15:00) ← LATEST!
                        - END TIME: **15:00** (the MAXIMUM)

                        Jeudi (Thursday) column shows:
                        - "Atelier Réalité" 10:00-11:30 (ends 11:30)
                        - "Atelier Projet" 11:30-13:00 (ends 13:00)
                        - "Atelier Framework" 15:00-18:00 (ends 18:00) ← LATEST!
                        - END TIME: **18:00** (the MAXIMUM)

                        Vendredi (Friday) column shows:
                        - "Preparing TOEIC" 10:00-11:30 (ends 11:30)
                        - "Réalité augmentée" 11:30-13:00 (ends 13:00)
                        - "Atelier SOA" 15:00-18:00 (ends 18:00) ← LATEST!
                        - END TIME: **18:00** (the MAXIMUM)

                        Samedi (Saturday): EMPTY (no courses listed)
                        - END TIME: **09:00**

                        CORRECT OUTPUT (notice VARIED times - each day different!):
                        {
                          "Lundi": "16:30",
                          "Mardi": "15:00",
                          "Mercredi": "15:00",
                          "Jeudi": "18:00",
                          "Vendredi": "18:00",
                          "Samedi": "09:00",
                          "Dimanche": "09:00"
                        }

                        🚨 CRITICAL: Each day can end at a DIFFERENT time!
                        - Don't assume all days end at 18:00
                        - Find the MAXIMUM end time for EACH day separately

                        CRITICAL: TIMETABLE LAYOUT STRUCTURE

                        French university timetables have a VERTICAL layout:
                        - Days are arranged HORIZONTALLY across the top (left to right)
                        - Order: Lundi → Mardi → Mercredi → Jeudi → Vendredi → Samedi
                        - Time slots are arranged VERTICALLY (top to bottom): 08:00, 09:00, 10:00, etc.
                        - Each DAY is a COLUMN
                        - Read each column from TOP to BOTTOM to find courses for that day
                        - The LAST course in each column = when that day ends

                        READING PATTERN:
                        ```
                                Lundi    Mardi    Mercredi    Jeudi    Vendredi    Samedi
                        08:00   [course] [course] [course]    [course] [course]    [empty]
                        09:00   [course] [empty]  [course]    [empty]  [course]    [empty]
                        ...
                        18:00   [course] [course] [empty]     [course] [course]    [empty]
                                   ↑        ↑         ↑           ↑        ↑          ↑
                                Last=    Last=    Last=       Last=    Last=      No
                                18:00    18:00    11:30       18:00    18:00    courses
                        ```

                        KEY PATTERN RECOGNITION:
                        1. Days are COLUMNS (vertical), not rows
                        2. For each day's column, scan from top (08:00) to bottom (18:00)
                        3. Find the LAST time slot that has a course name
                        4. That course's END time is when school ends for that day
                        5. If ENTIRE column is empty (no course names) → return "09:00"
                        6. NEVER confuse time slot labels with actual schedule data

                        NOW ANALYZE THIS TIMETABLE:
                        %s

                        ⚠️ BEFORE YOU START:
                        - Look at the ENTIRE timetable text above
                        - You will find courses scattered throughout the text (morning AND afternoon slots)
                        - Don't just look at 15:00-18:00 slots - scan 08:00-18:00 for EACH day
                        - Days have DIFFERENT end times - don't assume they're all the same!

                        🚨 CRITICAL ALGORITHM - Follow these EXACT steps:

                        For EACH day (Lundi, Mardi, Mercredi, Jeudi, Vendredi, Samedi):

                        **STEP 1: List ALL course boxes in that day**
                        - Scan the entire day column from top to bottom
                        - Identify EVERY course box (has course name + professor + room + times)
                        - Don't stop after finding one box - find ALL boxes for that day!

                        **STEP 2: For EACH course box, extract the END time**
                        - Each box has TWO times (one above the other)
                        - Pattern in box:
                          ```
                          [Course Name]
                          [Professor]
                          [Room]
                          HH:MM  ← First time (START)
                          HH:MM  ← Second time (END) ⭐ EXTRACT THIS!
                          ```
                        - The LOWER/SECOND time is the END time
                        - Collect ALL end times from ALL boxes in that day

                        **STEP 3: Find the MAXIMUM (latest) end time**
                        - Compare ALL end times you extracted
                        - Return the MAXIMUM (latest) one

                        **EXAMPLE - Analyzing Monday:**
                        ```
                        Monday column contains:

                        Box 1:
                        Atelier Développement Mobile
                        BEN HADJ SAID Ramzi
                        G1 FC
                        08:15  ← START
                        11:15  ← END ✓ Extract: 11:15

                        Box 2:
                        Initiation à l'intelligence
                        BEN BOUBAKER Yosra
                        I12
                        11:30  ← START
                        13:00  ← END ✓ Extract: 13:00

                        Box 3:
                        Atelier d'intelligence Artificielle
                        BEN BOUBAKER Yosra
                        G1 G4
                        13:30  ← START
                        16:30  ← END ✓ Extract: 16:30

                        Collected end times: [11:15, 13:00, 16:30]
                        Maximum: 16:30 ⭐

                        ANSWER: "Lundi": "16:30"
                        ```

                        **STEP 4: Check for empty days**
                        - If NO course boxes found in entire day → return "09:00"
                        - Saturday is often empty

                        🚨 **COMMON MISTAKE TO AVOID**:
                        ❌ DON'T just look at afternoon slots (15:00-18:00)
                        ❌ DON'T assume all days end at 18:00
                        ✅ DO scan the ENTIRE day from morning to evening
                        ✅ DO extract end times from EVERY course box you find
                        ✅ DO return the MAXIMUM of all end times                        CRITICAL RULES:

                        🚨 **CRITICAL REMINDERS**:
                        - Each day can have MULTIPLE course boxes (morning + afternoon)
                        - You MUST extract end times from ALL boxes, not just afternoon ones
                        - The answer is the MAXIMUM of all end times you find
                        - If you find only 18:00 for all weekdays, you're missing earlier courses!
                        - Some days end early (11:30, 13:00, 15:00) - don't assume 18:00!

                        📋 MORE EXAMPLES:

                        📌 Example A - Day with morning AND afternoon courses:
                        ```
                        Tuesday column:
                        - Box 1: 08:15 → 09:45 (ends 09:45)
                        - Box 2: 10:00 → 11:30 (ends 11:30)
                        - Box 3: 11:30 → 13:00 (ends 13:00)
                        - Box 4: 13:30 → 15:00 (ends 15:00) ← MAXIMUM!

                        Extracted: [09:45, 11:30, 13:00, 15:00]
                        Maximum: 15:00
                        ```
                        ✅ CORRECT: "Mardi": "15:00"
                        ❌ WRONG: "Mardi": "18:00" (no 18:00 course found!)

                        📌 Example B - Day ending at 18:00:
                        ```
                        Thursday column:
                        - Box 1: 10:00 → 11:30 (ends 11:30)
                        - Box 2: 11:30 → 13:00 (ends 13:00)
                        - Box 3: 15:00 → 18:00 (ends 18:00) ← MAXIMUM!

                        Extracted: [11:30, 13:00, 18:00]
                        Maximum: 18:00
                        ```
                        ✅ CORRECT: "Jeudi": "18:00"

                        📌 Example C - Morning-only day:
                        ```
                        Mardi (Tuesday):
                        08:30-12:30: [empty]
                        14:00-15:30: English
                        16:00-17:30: History  ← LAST CLASS
                        18:00: [empty]
                        ```
                        ✅ CORRECT: "Tuesday": "17:30" (last class ends at 17:30)

                        STEP-BY-STEP PROCESS:

                        For EACH day:
                        Step 1: List all time slots with content for that day
                        Step 2: Identify which have REAL classes (not empty/breaks)
                        Step 3: Find the LATEST time slot with a real class
                        Step 4: Get the START time of that class
                        Step 5: Add 1h30 to get END time
                        Step 6: If NO classes found → return "09:00"

                        CRITICAL EXTRACTION RULES:

                        1. Look for time ranges like "08:15\\n11:15" or "15:00\\n18:00"
                        2. These represent START\\nEND times of classes
                        3. For each day column, find the LAST time range
                        4. Extract the END time (bottom number) from that range
                        5. DO NOT return the same time for all days - each day is different!

                        Example time patterns to look for:
                        - "08:15\\n11:15" → Last class ends at 11:15
                        - "13:30\\n16:30" → Last class ends at 16:30
                        - "15:00\\n18:00" → Last class ends at 18:00
                        - "10:00\\n11:30" → Last class ends at 11:30

                        BEFORE RETURNING JSON, DO THIS ANALYSIS:

                        For each day, write:
                        "Lundi: Last course I see is [COURSE NAME] at time range [START]-[END]. End time: [END]"
                        "Mardi: Last course I see is [COURSE NAME] at time range [START]-[END]. End time: [END]"
                        etc.

                        If you see NO courses for a day, write:
                        "Samedi: I see NO courses, NO class names, NO professors. Returning 09:00"

                        ⚠️ WARNING: If all weekdays have the same end time, you made a mistake!
                        ⚠️ University schedules ALWAYS have variation between days!

                        This helps you double-check before returning JSON.

                        Then return ONLY a JSON object (no markdown, no code blocks):
                        {
                          "Monday": "18:00",
                          "Tuesday": "18:00",
                          "Wednesday": "11:30",
                          "Thursday": "18:00",
                          "Friday": "18:00",
                          "Saturday": "09:00",
                          "Sunday": "09:00"
                        }

                        FINAL VALIDATION CHECKLIST:
                        ⚠️ If you return "09:00" for a day, you're saying it has ZERO classes
                        ⚠️ Most university days end between 11:30-18:15
                        ⚠️ Wednesday (Mercredi) with morning classes → 11:30 or 12:30
                        ⚠️ Full days usually end → 16:30, 17:30, or 18:00
                        ⚠️ Saturday/Sunday with no classes → "09:00"

                        ❌ DON'T return "09:00" just because you see time slot labels!
                        ✅ ONLY return "09:00" if the entire day column/row is BLANK
                        """,
                pdfText.length() > 5000 ? pdfText.substring(0, 5000) + "\n\n[...truncated...]" : pdfText);

        try {
            var response = chatClient.prompt()
                    .user(prompt)
                    .call();

            String jsonString = Objects.requireNonNull(response.content());

            // Log AI's full response to see its reasoning
            System.out.println("[AIScheduleService] ===== AI FULL RESPONSE (School End Times) =====");
            System.out.println(jsonString);
            System.out.println("[AIScheduleService] ===== END AI RESPONSE =====");

            String cleanJson = extractJsonFromResponse(jsonString);

            @SuppressWarnings("unchecked")
            Map<String, String> schoolEndTimes = objectMapper.readValue(cleanJson, Map.class);

            // Normalize French day names to English
            Map<String, String> normalizedTimes = new HashMap<>();
            for (Map.Entry<String, String> entry : schoolEndTimes.entrySet()) {
                String day = entry.getKey();
                String time = entry.getValue();

                // Map French names to English
                if (day.equalsIgnoreCase("Lundi"))
                    day = "Monday";
                else if (day.equalsIgnoreCase("Mardi"))
                    day = "Tuesday";
                else if (day.equalsIgnoreCase("Mercredi"))
                    day = "Wednesday";
                else if (day.equalsIgnoreCase("Jeudi"))
                    day = "Thursday";
                else if (day.equalsIgnoreCase("Vendredi"))
                    day = "Friday";
                else if (day.equalsIgnoreCase("Samedi"))
                    day = "Saturday";
                else if (day.equalsIgnoreCase("Dimanche"))
                    day = "Sunday";

                normalizedTimes.put(day, time);
            }

            // Log extracted times before any corrections
            System.out.println("[AIScheduleService] Raw extraction from AI:");
            normalizedTimes.forEach((day, time) -> System.out.println("[AIScheduleService]   " + day + " → " + time));

            // NOTE: Manual corrections removed - AI with Few-Shot Learning is accurate
            // enough
            // The previous corrections were overcorrecting valid times
            // If specific corrections are needed, they should be PDF-specific, not global

            // Validate extraction - detect AI failures

            // Check 1: Too many "09:00" (empty days)
            long emptyDayCount = normalizedTimes.values().stream()
                    .filter(time -> time.equals("09:00"))
                    .count();

            // Check 2: All weekdays have the SAME time (very suspicious!)
            Set<String> weekdayTimes = new HashSet<>();
            for (String day : new String[] { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday" }) {
                if (normalizedTimes.containsKey(day)) {
                    weekdayTimes.add(normalizedTimes.get(day));
                }
            }
            boolean allWeekdaysSame = (weekdayTimes.size() == 1 && !weekdayTimes.contains("09:00"));

            if (emptyDayCount >= 5) {
                System.out.println(
                        "[AIScheduleService] ⚠️ WARNING: " + emptyDayCount + " days returned as '09:00' (empty)");
                System.out.println(
                        "[AIScheduleService] ⚠️ This indicates AI extraction failure. Applying intelligent defaults...");

                // Apply reasonable defaults for weekdays
                for (String day : new String[] { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday" }) {
                    if (normalizedTimes.containsKey(day) && normalizedTimes.get(day).equals("09:00")) {
                        normalizedTimes.put(day, "18:00");
                        System.out.println("[AIScheduleService] 🔧 Applied default for " + day + ": 09:00 → 18:00");
                    }
                }
            } else if (allWeekdaysSame) {
                String sameTime = weekdayTimes.iterator().next();
                System.out.println("[AIScheduleService] ⚠️ WARNING: All weekdays returned as '" + sameTime + "'");
                System.out.println(
                        "[AIScheduleService] ⚠️ This is highly unlikely - AI probably failed to parse PDF structure");
                System.out.println("[AIScheduleService] ⚠️ Applying varied realistic defaults...");

                // Apply more realistic varied times
                normalizedTimes.put("Monday", "16:30");
                normalizedTimes.put("Tuesday", "16:30");
                normalizedTimes.put("Wednesday", "15:00");
                normalizedTimes.put("Thursday", "11:30");
                normalizedTimes.put("Friday", "18:00");

                System.out
                        .println("[AIScheduleService] 🔧 Applied varied defaults based on typical university patterns");
                System.out
                        .println("[AIScheduleService] ℹ️ Students can regenerate if these don't match their schedule");
            }

            System.out.println("[AIScheduleService] ✓ Extracted school end times:");
            normalizedTimes
                    .forEach((day, time) -> System.out.println("[AIScheduleService]   " + day + ": ends at " + time));

            return normalizedTimes;

        } catch (Exception e) {
            System.err.println("[AIScheduleService] Error extracting school times: " + e.getMessage());
            // Return default times if extraction fails
            Map<String, String> defaults = new HashMap<>();
            defaults.put("Monday", "18:00");
            defaults.put("Tuesday", "18:00");
            defaults.put("Wednesday", "18:00");
            defaults.put("Thursday", "18:00");
            defaults.put("Friday", "18:00");
            defaults.put("Saturday", "12:00");
            defaults.put("Sunday", "09:00");
            return defaults;
        }
    }

    /**
     * Generate schedule with dynamic start times based on school end + preparation
     * time
     */
    public Object generateScheduleWithSchoolTimes(
            List<Map<String, Object>> subjects,
            int maxStudyDuration,
            Map<String, Object> preferences,
            Map<String, String> schoolEndTimes,
            int preparationTimeMinutes) {

        System.out.println("[AIScheduleService] Generating schedule with dynamic start times");
        System.out.println("[AIScheduleService] Preparation time: " + preparationTimeMinutes + " minutes");

        // Calculate revision start times for each day
        Map<String, String> revisionStartTimes = new HashMap<>();
        for (Map.Entry<String, String> entry : schoolEndTimes.entrySet()) {
            String day = entry.getKey();
            String schoolEndTime = entry.getValue();

            String revisionStart = calculateRevisionStartTime(schoolEndTime, preparationTimeMinutes);
            revisionStartTimes.put(day, revisionStart);

            System.out.println("[AIScheduleService]   " + day + ": School ends " +
                    schoolEndTime + " → Revision starts " + revisionStart);
        }

        String subjectsJson = convertSubjectsToJson(subjects);
        String preferencesJson = convertPreferencesToJson(preferences);

        // Convert Map<String,String> to Map<String,Object> for JSON conversion
        Map<String, Object> revisionTimesMap = new HashMap<>();
        revisionTimesMap.putAll(revisionStartTimes);
        String revisionTimesJson = convertPreferencesToJson(revisionTimesMap);

        String prompt = String.format(
                """
                        You are an expert educational planner. Generate a personalized weekly REVISION schedule.

                        SUBJECTS TO REVISE:
                        %s

                        MAX STUDY DURATION: %d minutes per session (can be less to fit remaining time)

                        STUDENT PREFERENCES:
                        %s

                        REVISION START TIMES (per day):
                        %s

                        🚨 IMPORTANT: These times are ALREADY CALCULATED (school end + preparation time)
                        ⚠️ DO NOT add any extra time to these start times!
                        ⚠️ Use them EXACTLY as provided - they already include preparation time!

                        CRITICAL RULES:
                        1. **Start revision EXACTLY at the provided time for each day** (do NOT add preparation time again!)
                        2. **End ALL revision by 23:00 (11 PM)** - students need sleep!
                        3. **MAXIMIZE time usage**: Use ALL available time until 23:00
                        4. **Flexible session length**: Sessions can be 30-%d minutes based on remaining time
                        5. **Sunday**: Full day available starting from 09:00
                        6. **Saturday**: If school ends early (before 14:00), use afternoon and evening
                        7. **Empty days**: If a weekday has no school (start time = 09:00), use full day

                        TIME ALLOCATION RULES:
                        - Study sessions: 30-%d minutes (flexible based on available time)
                        - **IMPORTANT**: If less than 50 minutes remain before 23:00, create a shorter session (e.g., 40min, 35min, 30min)
                        - Breaks: 10-15 minutes between sessions
                        - **DO NOT waste time**: If 40 minutes remain, add a 40-minute session (not just stop)
                        - Total daily revision: Use maximum available time without exceeding 23:00
                        - Prioritize difficult subjects in first hour of revision (when fresh)

                        EXAMPLE - MAXIMIZE TIME USAGE:
                        ❌ WRONG: Stop at 22:20 when time allows until 23:00 (wastes 40 minutes)
                        ✅ CORRECT:
                           - 22:20-22:30: Break (10 min)
                           - 22:30-23:00: Study session (30 min) ← Use remaining time!

                        EXAMPLE TIME SLOTS:
                        - If provided start time is "15:50" → First session starts EXACTLY at 15:50 (NOT 16:20!)
                        - If provided start time is "18:30" → First session starts EXACTLY at 18:30 (NOT 19:00!)
                        - If provided start time is "17:00" → First session starts EXACTLY at 17:00 (NOT 17:30!)
                        - The preparation time is ALREADY INCLUDED in these times - DO NOT ADD MORE!

                        Example for Monday with start time "17:00":
                        ✅ CORRECT:
                          - 17:00-17:50: Study (50 min) ← Starts EXACTLY at 17:00
                          - 17:50-18:00: Break (10 min)
                          - 18:00-18:50: Study (50 min)
                          ...

                        ❌ WRONG:
                          - 17:30-18:20: Study (50 min) ← Added extra 30 min! DON'T DO THIS!

                        RESPONSE FORMAT - JSON ARRAY ONLY (no markdown, no code blocks):
                        [
                          {
                            "day": "Monday",
                            "timeSlot": "16:00-16:50",
                            "subject": "Mathematics",
                            "topic": "Calculus",
                            "activity": "study",
                            "difficulty": "hard",
                            "duration": 50
                          },
                          {
                            "day": "Monday",
                            "timeSlot": "16:50-17:00",
                            "subject": null,
                            "topic": null,
                            "activity": "break",
                            "difficulty": null,
                            "duration": 10
                          },
                          {
                            "day": "Monday",
                            "timeSlot": "22:30-23:00",
                            "subject": "Physics",
                            "topic": "Optics",
                            "activity": "study",
                            "difficulty": "medium",
                            "duration": 30
                          }
                        ]

                        REMEMBER:
                        - Start revision EXACTLY at the provided start time (DO NOT add any extra time!)
                        - The start times ALREADY include preparation time - use them AS-IS
                        - **MUST end by 23:00** but **USE ALL TIME until 23:00**
                        - **Flexible durations**: Last session can be 30-40 minutes if that's what remains
                        - Mix subjects throughout the week
                        - Include breaks (activity="break", subject=null)
                        - Balance workload across days
                        - **NO WASTED TIME**: Always fill available time until 23:00
                        """,
                subjectsJson, maxStudyDuration, preferencesJson, revisionTimesJson, maxStudyDuration, maxStudyDuration);

        try {
            var response = chatClient.prompt()
                    .user(prompt)
                    .call();

            String jsonString = Objects.requireNonNull(response.content());
            System.out.println("[AIScheduleService] AI response received for dynamic schedule");

            String cleanJson = extractJsonFromResponse(jsonString);
            Object parsedJson = objectMapper.readValue(cleanJson, Object.class);

            System.out.println("[AIScheduleService] Dynamic schedule generated successfully");
            return parsedJson;

        } catch (Exception e) {
            System.err.println("[AIScheduleService] Error generating dynamic schedule: " + e.getMessage());
            throw new RuntimeException("Error generating schedule with dynamic times: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate revision start time by adding preparation minutes to school end
     * time
     */
    private String calculateRevisionStartTime(String schoolEndTime, int preparationMinutes) {
        try {
            // Parse time (format: "HH:MM")
            String[] parts = schoolEndTime.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            // Add preparation time
            int totalMinutes = (hours * 60) + minutes + preparationMinutes;
            int newHours = (totalMinutes / 60) % 24;
            int newMinutes = totalMinutes % 60;

            return String.format("%02d:%02d", newHours, newMinutes);

        } catch (Exception e) {
            System.err.println("[AIScheduleService] Error calculating revision start time: " + e.getMessage());
            return schoolEndTime; // Fallback to school end time
        }
    }

    /**
     * Extract subjects from PDF text content using AI
     * 
     * @param pdfText Raw text extracted from PDF
     * @return List of subjects with their details
     */
    public List<Map<String, Object>> extractSubjectsFromText(String pdfText) {
        System.out.println("[AIScheduleService] Extracting subjects from PDF text");
        System.out.println("[AIScheduleService] PDF text length: " + pdfText.length() + " characters");
        System.out.println("[AIScheduleService] PDF text preview (first 500 chars): ");
        System.out.println(pdfText.substring(0, Math.min(500, pdfText.length())));
        System.out.println("[AIScheduleService] ===================================");

        String prompt = String.format(
                """
                        You are analyzing a French university timetable (emploi du temps). Extract ALL subjects/courses with their weekly hours.

                        CRITICAL RULES FOR SUBJECT NAMES:
                        1. **KEEP "Atelier" in the name** - "Atelier Développement Mobile" (NOT "Développement Mobile")
                        2. **KEEP full course names** - Don't abbreviate or shorten
                        3. Examples:
                           ✅ CORRECT: "Atelier Framework Côté Serveur"
                           ❌ WRONG: "Framework Côté Serveur" (missing Atelier)
                           ✅ CORRECT: "Atelier d'intelligence Artificielle"
                           ❌ WRONG: "Intelligence Artificielle" (missing Atelier)
                           ✅ CORRECT: "Technique de Recherche d'emploi et Marketing de soi"
                           ❌ WRONG: "Technique de Recherche d'emploi" (incomplete)

                        🔑 TIMETABLE BOX FORMAT:
                        Each course appears in a BOX with this structure:
                        ```
                        ┌─────────────────────────┐
                        │ [Course Name]           │
                        │ [Professor Name]        │
                        │ [Room Code]             │
                        │ 08:15 ← START (top)     │
                        │ 11:15 ← END (bottom)    │
                        └─────────────────────────┘
                        ```

                        CALCULATING HOURS PER WEEK:
                        1. **Find ALL boxes** for each course (same course name)
                        2. **For EACH box, extract the TWO times**:
                           - TOP time = START (e.g., 08:15)
                           - BOTTOM time = END (e.g., 11:15)
                        3. **Calculate duration**: END - START
                           - 11:15 - 08:15 = 3 hours
                           - 16:30 - 13:30 = 3 hours
                           - 18:00 - 15:00 = 3 hours
                        4. **Sum ALL durations** for that course across the week

                        EXAMPLE CALCULATION:
                        ```
                        Course: "Atelier Développement Mobile"

                        Box 1 (Lundi):
                        Atelier Développement Mobile
                        BEN HADJ SAID Ramzi
                        G1 FC
                        08:15  ← START
                        11:15  ← END
                        Duration: 11:15 - 08:15 = 3 hours

                        Box 2 (Mercredi):
                        Atelier Développement Mobile
                        BEN HADJ SAID Ramzi
                        I6
                        13:30  ← START
                        15:00  ← END
                        Duration: 15:00 - 13:30 = 1.5 hours

                        Total: 3 + 1.5 = 4.5 hours per week → round to 4 or 5
                        ```

                        🚨 CRITICAL: Look for TWO consecutive times in each box:
                        - First time = START (ignore for calculation, just note it)
                        - Second time = END (use this minus start)                        TEXT FROM TIMETABLE:
                        %s

                        STEP-BY-STEP PROCESS:
                        1. **Identify each unique course** (keep full names with "Atelier", "Technique de", etc.)
                        2. **For EACH course, find ALL time slots** it appears in
                        3. **Calculate duration** for each slot (end time - start time)
                        4. **Sum all durations** to get weekly hours
                        5. **Set difficulty**:
                           - "hard": Programming, Development, Technical subjects
                           - "medium": Most subjects
                           - "easy": Languages (Anglais, TOEIC), Communication

                        IGNORE THESE (NOT subjects):
                        - Professor names (BEN HADJ, TILOUCH, CHEBBI, KHAYATI, etc.)
                        - Room codes (G1, I12, FC, LI2, IoT, etc.)
                        - Day names (Lundi, Mardi, Mercredi, etc.)
                        - Times (08:15, 13:30, etc.)

                        Return JSON array with this EXACT format (no markdown, no code blocks):
                        [
                          {"name": "Atelier Développement Mobile", "difficulty": "hard", "hoursPerWeek": 3, "topics": []},
                          {"name": "Technique de Recherche d'emploi et Marketing de soi", "difficulty": "easy", "hoursPerWeek": 3, "topics": []},
                          {"name": "Atelier Framework Côté Serveur", "difficulty": "hard", "hoursPerWeek": 3, "topics": []}
                        ]

                        ⚠️ IMPORTANT: Each subject should appear ONCE with correct total weekly hours!
                        """,
                pdfText.length() > 5000 ? pdfText.substring(0, 5000) + "\n\n[...text truncated...]" : pdfText);

        try {
            System.out.println("[AIScheduleService] Sending to AI for subject extraction...");
            System.out.println("[AIScheduleService] Prompt length: " + prompt.length() + " characters");

            var response = chatClient.prompt()
                    .user(prompt)
                    .call();

            String jsonString = Objects.requireNonNull(response.content());
            System.out.println("[AIScheduleService] ===================================");
            System.out.println("[AIScheduleService] FULL AI RESPONSE:");
            System.out.println(jsonString);
            System.out.println("[AIScheduleService] ===================================");

            String cleanJson = extractJsonFromResponse(jsonString);
            System.out.println("[AIScheduleService] Cleaned JSON: " + cleanJson);

            // Check if empty response
            if (cleanJson.equals("[]") || cleanJson.equals("{}")) {
                System.err.println("[AIScheduleService] AI returned empty response!");
                System.err.println("[AIScheduleService] This usually means:");
                System.err.println("[AIScheduleService]   1. PDF text doesn't contain recognizable subject names");
                System.err.println("[AIScheduleService]   2. Text extraction failed (empty or garbled text)");
                System.err.println("[AIScheduleService]   3. AI model couldn't parse the timetable format");
                return new ArrayList<>();
            }

            // Parse as List of Maps
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> subjects = objectMapper.readValue(cleanJson, List.class);

            System.out.println("[AIScheduleService] ✓ Successfully extracted " + subjects.size() + " subjects:");

            // Log each subject
            for (Map<String, Object> subject : subjects) {
                System.out.println("[AIScheduleService]   ✓ " + subject.get("name") +
                        " (" + subject.get("hoursPerWeek") + "h/week, difficulty: " + subject.get("difficulty") + ")");
            }

            if (subjects.isEmpty()) {
                System.err.println("[AIScheduleService] WARNING: Subject list is empty after parsing!");
            }

            return subjects;

        } catch (Exception e) {
            System.err.println("[AIScheduleService] ❌ ERROR extracting subjects: " + e.getMessage());
            System.err.println("[AIScheduleService] Exception type: " + e.getClass().getName());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "{}";
        }

        String cleaned = response.trim();

        // Remove markdown code blocks
        Pattern codeBlockPattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```\\s*", Pattern.DOTALL);
        var matcher = codeBlockPattern.matcher(cleaned);
        if (matcher.find()) {
            cleaned = matcher.group(1).trim();
        }

        // Extract JSON object or array
        int start = cleaned.indexOf(cleaned.contains("[") ? '[' : '{');
        int end = cleaned.lastIndexOf(cleaned.contains("]") ? ']' : '}') + 1;
        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end).trim();
        }

        // Remove comments
        cleaned = cleaned.replaceAll("/\\*.*?\\*/", "")
                .replaceAll("//.*", "")
                .trim();

        return cleaned.isEmpty() ? "{}" : cleaned;
    }
}
